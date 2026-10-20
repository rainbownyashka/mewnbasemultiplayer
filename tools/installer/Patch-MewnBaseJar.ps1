[CmdletBinding()]
param(
  [Parameter(Mandatory = $true)]
  [ValidateSet('Install', 'Restore')]
  [string]$Action,

  [Parameter(Mandatory = $true)]
  [string]$GameDir,

  [Parameter(Mandatory = $true)]
  [string]$PayloadZip
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem
$logPath = Join-Path $env:TEMP 'MewnBaseMP-Setup.log'
try { Start-Transcript -Path $logPath -Append | Out-Null } catch { }
trap {
  try { Add-Content -LiteralPath $logPath -Value ("ERROR: " + $_.Exception.Message) -Encoding UTF8 } catch { }
  try { Stop-Transcript | Out-Null } catch { }
  exit 1
}

function Get-StreamSha256([IO.Stream]$Stream) {
  $sha = [Security.Cryptography.SHA256]::Create()
  try {
    return [BitConverter]::ToString($sha.ComputeHash($Stream)).Replace('-', '').ToLowerInvariant()
  } finally {
    $sha.Dispose()
  }
}

function Get-FileSha256([string]$Path) {
  $stream = [IO.File]::OpenRead($Path)
  try { return Get-StreamSha256 $stream }
  finally { $stream.Dispose() }
}

function Get-EntryBytes([IO.Compression.ZipArchiveEntry]$Entry) {
  $inputStream = $Entry.Open()
  $memory = New-Object IO.MemoryStream
  try {
    $inputStream.CopyTo($memory)
    return ,$memory.ToArray()
  } finally {
    $inputStream.Dispose()
    $memory.Dispose()
  }
}

function Assert-GameClosed([string]$Root, [string]$Jar) {
  $rootFull = [IO.Path]::GetFullPath($Root).TrimEnd('\')
  $jarFull = [IO.Path]::GetFullPath($Jar)
  try {
    $processes = Get-CimInstance Win32_Process -ErrorAction Stop
  } catch {
    throw "Cannot check whether MewnBase is running. Close the game, then retry. Details: $($_.Exception.Message)"
  }
  foreach ($process in $processes) {
    if ($process.Name -eq 'MewnBase.exe' -and $process.ExecutablePath) {
      if ([string]::Equals([IO.Path]::GetFullPath($process.ExecutablePath), (Join-Path $rootFull 'MewnBase.exe'), [StringComparison]::OrdinalIgnoreCase)) {
        throw 'Close MewnBase before installing or restoring the patch.'
      }
    }
    if (($process.Name -eq 'java.exe' -or $process.Name -eq 'javaw.exe') -and $process.CommandLine) {
      if ($process.CommandLine.IndexOf($jarFull, [StringComparison]::OrdinalIgnoreCase) -ge 0) {
        throw 'Close MewnBase before installing or restoring the patch.'
      }
    }
  }
}

function Replace-FileSafely([string]$Source, [string]$Destination) {
  $rollback = $Destination + '.' + [guid]::NewGuid().ToString('N') + '.rollback'
  try {
    [IO.File]::Replace($Source, $Destination, $rollback, $true)
  } catch {
    if (Test-Path -LiteralPath $rollback) {
      throw "The replacement operation failed and left a rollback file at '$rollback'. Details: $($_.Exception.Message)"
    }
    Move-Item -LiteralPath $Destination -Destination $rollback
    try {
      Move-Item -LiteralPath $Source -Destination $Destination
    } catch {
      if (Test-Path -LiteralPath $rollback) { Move-Item -LiteralPath $rollback -Destination $Destination }
      throw
    }
  }
  if (Test-Path -LiteralPath $rollback) { Remove-Item -LiteralPath $rollback -Force }
}

function Read-PatchPackage([string]$Path) {
  $archive = [IO.Compression.ZipFile]::OpenRead($Path)
  try {
    $manifestEntry = $archive.GetEntry('patch-manifest.json')
    if ($null -eq $manifestEntry) { throw 'Patch package is missing patch-manifest.json.' }
    $reader = New-Object IO.StreamReader($manifestEntry.Open(), [Text.Encoding]::UTF8, $true)
    try { $manifest = $reader.ReadToEnd() | ConvertFrom-Json }
    finally { $reader.Dispose() }
    if ($manifest.schemaVersion -ne 1 -or -not $manifest.baseJarSha256 -or -not $manifest.entries) {
      throw 'Patch package has an unsupported or incomplete manifest.'
    }

    $entryHashes = New-Object 'System.Collections.Generic.Dictionary[string,string]' ([StringComparer]::Ordinal)
    foreach ($expected in $manifest.entries) {
      $name = [string]$expected.path
      if (-not $name.EndsWith('.class', [StringComparison]::Ordinal) -or
          $name.Contains('..') -or $name.StartsWith('/') -or $name.Contains('\')) {
        throw "Patch manifest contains an unsafe class path: $name"
      }
      if ($entryHashes.ContainsKey($name)) { throw "Patch manifest repeats a class path: $name" }
      $entryHashes.Add($name, [string]$expected.sha256)
    }
    if ($entryHashes.Count -ne [int]$manifest.entryCount) {
      throw 'Patch manifest entry count does not match its class list.'
    }

    $patchBytes = New-Object 'System.Collections.Generic.Dictionary[string,byte[]]' ([StringComparer]::Ordinal)
    $actualPayloadNames = New-Object 'System.Collections.Generic.HashSet[string]' ([StringComparer]::Ordinal)
    foreach ($entry in $archive.Entries) {
      if ($entry.FullName -eq 'patch-manifest.json') { continue }
      if (-not $entry.FullName.EndsWith('.class', [StringComparison]::Ordinal)) {
        throw "Unexpected non-class file in patch package: $($entry.FullName)"
      }
      if (-not $actualPayloadNames.Add($entry.FullName)) { throw "Patch package repeats a class: $($entry.FullName)" }
      if (-not $entryHashes.ContainsKey($entry.FullName)) { throw "Patch package contains a class absent from its manifest: $($entry.FullName)" }
      $bytes = Get-EntryBytes $entry
      $memory = New-Object IO.MemoryStream(, $bytes)
      try { $actualHash = Get-StreamSha256 $memory }
      finally { $memory.Dispose() }
      if ($actualHash -ne $entryHashes[$entry.FullName]) { throw "Patch class failed its SHA-256 check: $($entry.FullName)" }
      $patchBytes.Add($entry.FullName, $bytes)
    }
    if ($patchBytes.Count -ne $entryHashes.Count) { throw 'Patch package is missing one or more class files from its manifest.' }
    return [pscustomobject]@{ Manifest = $manifest; Entries = $patchBytes; Expected = $entryHashes }
  } finally { $archive.Dispose() }
}

function Get-ExistingBackup([string]$Path, [string]$ExpectedSha256) {
  if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) { return $false }
  $actual = Get-FileSha256 $Path
  if ($actual -ne $ExpectedSha256) {
    throw "The reserved original backup exists but does not match the supported game version: $Path. It was left untouched."
  }
  return $true
}

$root = [IO.Path]::GetFullPath($GameDir).TrimEnd('\')
$jarPath = Join-Path $root 'game\desktop-1.0.jar'
$backupPath = Join-Path $root 'game\desktop-1.0_original.jar'
$markerPath = Join-Path $root 'game\mewnbase-mp-patch.json'
$payloadPath = (Resolve-Path -LiteralPath $PayloadZip).Path

if (-not (Test-Path -LiteralPath $root -PathType Container) -or
    -not (Test-Path -LiteralPath (Join-Path $root 'MewnBase.exe') -PathType Leaf) -or
    -not (Test-Path -LiteralPath (Join-Path $root 'data') -PathType Container) -or
    -not (Test-Path -LiteralPath $jarPath -PathType Leaf)) {
  throw 'This folder is not a compatible MewnBase installation. Select the folder containing MewnBase.exe, data, and game\desktop-1.0.jar.'
}

$package = Read-PatchPackage $payloadPath
$expectedBaseHash = ([string]$package.Manifest.baseJarSha256).ToLowerInvariant()
if ($expectedBaseHash -notmatch '^[0-9a-f]{64}$') { throw 'Patch package has an invalid base JAR hash.' }

if ($Action -eq 'Restore') {
  if (-not (Get-ExistingBackup $backupPath $expectedBaseHash)) {
    throw "The original game backup was not found: $backupPath"
  }
  Assert-GameClosed $root $jarPath
  $currentHash = Get-FileSha256 $jarPath
  if ($currentHash -eq $expectedBaseHash) {
    if (Test-Path -LiteralPath $markerPath) { Remove-Item -LiteralPath $markerPath -Force }
    Write-Host "The game JAR is already original. The backup remains at: $backupPath"
    exit 0
  }
  if (-not (Test-Path -LiteralPath $markerPath -PathType Leaf)) {
    throw 'The game JAR changed after the backup was made and has no matching installer marker. It was left untouched.'
  }
  $marker = Get-Content -LiteralPath $markerPath -Raw | ConvertFrom-Json
  if ($marker.baseJarSha256 -ne $expectedBaseHash -or $marker.patchedJarSha256 -ne $currentHash) {
    throw 'The game JAR no longer matches the installed patch. It was left untouched.'
  }
  $restoreTemp = $jarPath + '.' + [guid]::NewGuid().ToString('N') + '.restore'
  try {
    Copy-Item -LiteralPath $backupPath -Destination $restoreTemp
    if ((Get-FileSha256 $restoreTemp) -ne $expectedBaseHash) { throw 'The staged original backup failed its SHA-256 check.' }
    Replace-FileSafely $restoreTemp $jarPath
    if (Test-Path -LiteralPath $markerPath) { Remove-Item -LiteralPath $markerPath -Force }
  } finally {
    if (Test-Path -LiteralPath $restoreTemp) { Remove-Item -LiteralPath $restoreTemp -Force }
  }
  Write-Host "Restored the original game JAR. The backup remains at: $backupPath"
  exit 0
}

Assert-GameClosed $root $jarPath
$currentHash = Get-FileSha256 $jarPath
$hasBackup = Get-ExistingBackup $backupPath $expectedBaseHash
if (-not $hasBackup) {
  if ($currentHash -ne $expectedBaseHash) {
    throw "This MewnBase JAR is not the supported original version. No backup or replacement was made. Expected SHA-256 $expectedBaseHash; found $currentHash."
  }
  Copy-Item -LiteralPath $jarPath -Destination $backupPath
  if ((Get-FileSha256 $backupPath) -ne $expectedBaseHash) {
    Remove-Item -LiteralPath $backupPath -Force
    throw 'The original JAR backup could not be verified; installation was stopped.'
  }
} elseif ($currentHash -ne $expectedBaseHash) {
  if (-not (Test-Path -LiteralPath $markerPath -PathType Leaf)) {
    throw 'The game JAR differs from the original backup and has no matching installer marker. It was left untouched.'
  }
  $oldMarker = Get-Content -LiteralPath $markerPath -Raw | ConvertFrom-Json
  if ($oldMarker.baseJarSha256 -ne $expectedBaseHash -or $oldMarker.patchedJarSha256 -ne $currentHash) {
    throw 'The game JAR changed after installation. It was left untouched; verify the game files in Steam/itch and retry.'
  }
}

$tempJar = $jarPath + '.' + [guid]::NewGuid().ToString('N') + '.tmp'
$markerTemp = $markerPath + '.' + [guid]::NewGuid().ToString('N') + '.tmp'
$sourceArchive = $null
$targetArchive = $null
$targetStream = $null
$patchArchive = $null
try {
  $patchArchive = [IO.Compression.ZipFile]::OpenRead($payloadPath)
  foreach ($entry in $patchArchive.Entries) {
    if ($entry.FullName.ToUpperInvariant().StartsWith('META-INF/') -and
        ($entry.FullName.ToUpperInvariant().EndsWith('.SF') -or
         $entry.FullName.ToUpperInvariant().EndsWith('.RSA') -or
         $entry.FullName.ToUpperInvariant().EndsWith('.DSA') -or
         $entry.FullName.ToUpperInvariant().EndsWith('.EC'))) {
      throw 'Signed JARs are not supported because a class patch would invalidate their signature.'
    }
  }
  $patchArchive.Dispose(); $patchArchive = $null

  $sourceArchive = [IO.Compression.ZipFile]::OpenRead($backupPath)
  $signatureEntries = @($sourceArchive.Entries | Where-Object {
    $upperName = $_.FullName.ToUpperInvariant()
    $upperName.StartsWith('META-INF/') -and
      ($upperName.EndsWith('.SF') -or $upperName.EndsWith('.RSA') -or $upperName.EndsWith('.DSA') -or $upperName.EndsWith('.EC'))
  })
  if ($signatureEntries.Count -gt 0) { throw 'The original game JAR is signed; a class patch would invalidate its signature.' }

  $targetStream = [IO.File]::Open($tempJar, [IO.FileMode]::CreateNew, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None)
  $targetArchive = New-Object IO.Compression.ZipArchive($targetStream, [IO.Compression.ZipArchiveMode]::Create, $false)
  $replacedNames = New-Object 'System.Collections.Generic.HashSet[string]' ([StringComparer]::Ordinal)
  foreach ($entry in $sourceArchive.Entries) {
    if ($package.Entries.ContainsKey($entry.FullName)) { continue }
    $copy = $targetArchive.CreateEntry($entry.FullName, [IO.Compression.CompressionLevel]::Optimal)
    if ($entry.LastWriteTime.Year -ge 1980 -and $entry.LastWriteTime.Year -le 2107) {
      $copy.LastWriteTime = $entry.LastWriteTime
    } else {
      $copy.LastWriteTime = [DateTimeOffset]::new(1980, 1, 1, 0, 0, 0, [TimeSpan]::Zero)
    }
    $copy.ExternalAttributes = $entry.ExternalAttributes
    $input = $entry.Open()
    $output = $copy.Open()
    try { $input.CopyTo($output) }
    finally { $output.Dispose(); $input.Dispose() }
  }
  foreach ($name in ($package.Entries.Keys | Sort-Object)) {
    $zipEntry = $targetArchive.CreateEntry($name, [IO.Compression.CompressionLevel]::Optimal)
    $zipEntry.LastWriteTime = [DateTimeOffset]::new(1980, 1, 1, 0, 0, 0, [TimeSpan]::Zero)
    $output = $zipEntry.Open()
    try { $output.Write($package.Entries[$name], 0, $package.Entries[$name].Length) }
    finally { $output.Dispose() }
  }
  $targetArchive.Dispose(); $targetArchive = $null
  $targetStream.Dispose(); $targetStream = $null
  $sourceArchive.Dispose(); $sourceArchive = $null

  $verifyArchive = [IO.Compression.ZipFile]::OpenRead($tempJar)
  try {
    $counts = New-Object 'System.Collections.Generic.Dictionary[string,int]' ([StringComparer]::Ordinal)
    foreach ($entry in $verifyArchive.Entries) {
      if ($package.Entries.ContainsKey($entry.FullName)) {
        if ($counts.ContainsKey($entry.FullName)) { $counts[$entry.FullName]++ } else { $counts.Add($entry.FullName, 1) }
        $stream = $entry.Open()
        try { $actualHash = Get-StreamSha256 $stream }
        finally { $stream.Dispose() }
        if ($actualHash -ne $package.Expected[$entry.FullName]) {
          throw "Staged JAR class failed SHA-256 verification: $($entry.FullName)"
        }
      }
    }
    if ($counts.Count -ne $package.Entries.Count) { throw 'The staged JAR is missing one or more patched classes.' }
    foreach ($name in $package.Entries.Keys) {
      if ($counts[$name] -ne 1) { throw "The staged JAR contains an unexpected duplicate for patched class: $name" }
    }
    if ($null -eq $verifyArchive.GetEntry('META-INF/MANIFEST.MF')) { throw 'The staged JAR has no manifest.' }
  } finally { $verifyArchive.Dispose() }

  $patchedHash = Get-FileSha256 $tempJar
  $marker = [ordered]@{
    schemaVersion = 1
    patchVersion = [string]$package.Manifest.patchVersion
    baseJarSha256 = $expectedBaseHash
    patchedJarSha256 = $patchedHash
    backupFile = 'game\desktop-1.0_original.jar'
  }
  [IO.File]::WriteAllText($markerTemp, ($marker | ConvertTo-Json -Depth 3), (New-Object Text.UTF8Encoding($false)))

  Replace-FileSafely $tempJar $jarPath
  try {
    if (Test-Path -LiteralPath $markerPath) { Remove-Item -LiteralPath $markerPath -Force }
    Move-Item -LiteralPath $markerTemp -Destination $markerPath
  } catch {
    Copy-Item -LiteralPath $backupPath -Destination $jarPath -Force
    throw "The patch could not be recorded; the original game JAR was restored from backup. Details: $($_.Exception.Message)"
  }
} finally {
  if ($null -ne $patchArchive) { $patchArchive.Dispose() }
  if ($null -ne $sourceArchive) { $sourceArchive.Dispose() }
  if ($null -ne $targetArchive) { $targetArchive.Dispose() }
  if ($null -ne $targetStream) { $targetStream.Dispose() }
  if (Test-Path -LiteralPath $tempJar) { Remove-Item -LiteralPath $tempJar -Force }
  if (Test-Path -LiteralPath $markerTemp) { Remove-Item -LiteralPath $markerTemp -Force }
}

Write-Host "Installed multiplayer patch $($package.Manifest.patchVersion)."
Write-Host "Original JAR backup: $backupPath"
Write-Host "Patched JAR SHA-256: $(Get-FileSha256 $jarPath)"
