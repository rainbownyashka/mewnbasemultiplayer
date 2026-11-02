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
  $failureMessage = $_.Exception.Message
  try { Stop-Transcript | Out-Null } catch { }
  try { Add-Content -LiteralPath $logPath -Value ("ERROR: " + $failureMessage) -Encoding UTF8 } catch { }
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

function Read-PatchContainer([string]$Path) {
  $archive = [IO.Compression.ZipFile]::OpenRead($Path)
  $temporaryDirectory = Join-Path $env:TEMP ('MewnBaseMP-payload-' + [guid]::NewGuid().ToString('N'))
  try {
    $outerNames = New-Object 'System.Collections.Generic.HashSet[string]' ([StringComparer]::Ordinal)
    foreach ($entry in $archive.Entries) {
      if (-not $outerNames.Add($entry.FullName)) { throw "Patch container contains a duplicate path: $($entry.FullName)" }
    }

    if ($null -ne $archive.GetEntry('patch-manifest.json')) {
      $package = Read-PatchPackage $Path
      return [pscustomobject]@{
        PatchVersion = [string]$package.Manifest.patchVersion
        Variants = @([pscustomobject]@{
          GameVersion = [string]$package.Manifest.gameVersion
          BaseJarSha256 = ([string]$package.Manifest.baseJarSha256).ToLowerInvariant()
          Package = $package
        })
      }
    }

    $bundleEntry = $archive.GetEntry('bundle-manifest.json')
    if ($null -eq $bundleEntry) { throw 'Payload ZIP is neither a flat class patch nor a versioned patch bundle.' }
    $reader = New-Object IO.StreamReader($bundleEntry.Open(), [Text.Encoding]::UTF8, $true)
    try { $bundle = $reader.ReadToEnd() | ConvertFrom-Json }
    finally { $reader.Dispose() }
    $items = @($bundle.variants)
    if ($bundle.schemaVersion -ne 1 -or -not [string]$bundle.patchVersion -or $items.Count -lt 2 -or
        $archive.Entries.Count -ne ($items.Count + 1)) {
      throw 'Patch bundle has an unsupported or incomplete manifest.'
    }

    New-Item -ItemType Directory -Path $temporaryDirectory | Out-Null
    $versions = New-Object 'System.Collections.Generic.HashSet[string]' ([StringComparer]::Ordinal)
    $baseHashes = New-Object 'System.Collections.Generic.HashSet[string]' ([StringComparer]::OrdinalIgnoreCase)
    $paths = New-Object 'System.Collections.Generic.HashSet[string]' ([StringComparer]::Ordinal)
    $variants = New-Object 'System.Collections.Generic.List[object]'
    foreach ($item in $items) {
      $gameVersion = [string]$item.gameVersion
      $baseHash = ([string]$item.baseJarSha256).ToLowerInvariant()
      $payloadHash = ([string]$item.payloadSha256).ToLowerInvariant()
      $payloadName = [string]$item.payloadPath
      if ($gameVersion -notmatch '^\d+\.\d+\.\d+$' -or -not $versions.Add($gameVersion) -or
          $baseHash -notmatch '^[0-9a-f]{64}$' -or -not $baseHashes.Add($baseHash) -or
          $payloadHash -notmatch '^[0-9a-f]{64}$' -or
          -not $payloadName.StartsWith('payloads/', [StringComparison]::Ordinal) -or
          -not $payloadName.EndsWith('.zip', [StringComparison]::Ordinal) -or
          $payloadName.Contains('..') -or $payloadName.Contains('\') -or -not $paths.Add($payloadName)) {
        throw "Patch bundle contains an invalid or duplicate variant for game $gameVersion."
      }
      $payloadEntry = $archive.GetEntry($payloadName)
      if ($null -eq $payloadEntry) { throw "Patch bundle is missing payload $payloadName." }
      $payloadBytes = Get-EntryBytes $payloadEntry
      $payloadMemory = New-Object IO.MemoryStream(, $payloadBytes)
      try { $actualPayloadHash = Get-StreamSha256 $payloadMemory }
      finally { $payloadMemory.Dispose() }
      if ($actualPayloadHash -ne $payloadHash) { throw "Bundled payload hash mismatch: $payloadName" }

      $payloadPath = Join-Path $temporaryDirectory (([guid]::NewGuid().ToString('N')) + '.zip')
      [IO.File]::WriteAllBytes($payloadPath, $payloadBytes)
      $package = Read-PatchPackage $payloadPath
      if ([string]$package.Manifest.patchVersion -ne [string]$bundle.patchVersion -or
          [string]$package.Manifest.gameVersion -ne $gameVersion -or
          ([string]$package.Manifest.baseJarSha256).ToLowerInvariant() -ne $baseHash -or
          [int]$package.Manifest.entryCount -ne [int]$item.entryCount -or
          ([string]$package.Manifest.patchedRuntimeJarSha256).ToLowerInvariant() -ne ([string]$item.patchedRuntimeJarSha256).ToLowerInvariant()) {
        throw "Bundle manifest does not match nested payload $payloadName."
      }
      $variants.Add([pscustomobject]@{ GameVersion = $gameVersion; BaseJarSha256 = $baseHash; Package = $package })
    }

    foreach ($entry in $archive.Entries) {
      if ($entry.FullName -ne 'bundle-manifest.json' -and -not $paths.Contains($entry.FullName)) {
        throw "Patch bundle contains an unexpected file: $($entry.FullName)"
      }
    }
    if (-not $versions.Contains('1.0.1') -or -not $versions.Contains('1.0.2')) {
      throw 'This patch bundle does not support both MewnBase 1.0.1 and 1.0.2.'
    }
    return [pscustomobject]@{ PatchVersion = [string]$bundle.patchVersion; Variants = @($variants.ToArray()) }
  } finally {
    $archive.Dispose()
    if (Test-Path -LiteralPath $temporaryDirectory) { Remove-Item -LiteralPath $temporaryDirectory -Recurse -Force }
  }
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

$container = Read-PatchContainer $payloadPath
$currentHash = Get-FileSha256 $jarPath
$initialCurrentHash = $currentHash
$hasBackup = Test-Path -LiteralPath $backupPath -PathType Leaf
$backupHash = ''
if ($hasBackup) { $backupHash = Get-FileSha256 $backupPath }
$initialBackupHash = $backupHash

$marker = $null
$hasMarker = Test-Path -LiteralPath $markerPath -PathType Leaf
if ($hasMarker) {
  try { $marker = Get-Content -LiteralPath $markerPath -Raw | ConvertFrom-Json }
  catch { throw 'The installer marker is unreadable. The game JAR and backup were left untouched.' }
}

if ($hasBackup) {
  $matchingVariant = @($container.Variants | Where-Object { $_.BaseJarSha256 -eq $backupHash })
  if ($matchingVariant.Count -ne 1) {
    throw "The original backup does not match a supported game version. It was left untouched: $backupPath"
  }
  if ($currentHash -ne $backupHash) {
    if ($null -eq $marker -or
        ([string]$marker.baseJarSha256).ToLowerInvariant() -ne $backupHash -or
        ([string]$marker.patchedJarSha256).ToLowerInvariant() -ne $currentHash) {
      throw 'The game JAR differs from its original backup and has no matching installer marker. It was left untouched.'
    }
  } elseif ($null -ne $marker -and
      ([string]$marker.baseJarSha256).ToLowerInvariant() -ne $backupHash) {
    throw 'The installer marker refers to a different original game version. The JAR and backup were left untouched.'
  }
} else {
  if ($null -ne $marker) { throw 'An installer marker exists without its required original backup. The game JAR was left untouched.' }
  $matchingVariant = @($container.Variants | Where-Object { $_.BaseJarSha256 -eq $currentHash })
  if ($matchingVariant.Count -ne 1) {
    $supportedHashes = ($container.Variants | ForEach-Object { $_.BaseJarSha256 }) -join ', '
    throw "This MewnBase JAR is not a supported original version. No backup or replacement was made. Found $currentHash; supported baselines: $supportedHashes."
  }
}

$selectedVariant = $matchingVariant[0]
$package = $selectedVariant.Package
$expectedBaseHash = [string]$selectedVariant.BaseJarSha256
if ($expectedBaseHash -notmatch '^[0-9a-f]{64}$' -or
    ([string]$package.Manifest.baseJarSha256).ToLowerInvariant() -ne $expectedBaseHash) {
  throw 'The selected patch package does not match the detected original game JAR.'
}

if ($Action -eq 'Restore') {
  if (-not $hasBackup) {
    throw "The original game backup was not found: $backupPath"
  }
  Assert-GameClosed $root $jarPath
  if ((Get-FileSha256 $jarPath) -ne $initialCurrentHash) {
    throw 'The game JAR changed during restore validation. It was left untouched.'
  }
  if ((Get-FileSha256 $backupPath) -ne $expectedBaseHash) {
    throw 'The original JAR backup changed during restore validation. It was left untouched.'
  }
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
  if (([string]$marker.baseJarSha256).ToLowerInvariant() -ne $expectedBaseHash -or
      ([string]$marker.patchedJarSha256).ToLowerInvariant() -ne $currentHash) {
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
if ($currentHash -ne $initialCurrentHash) {
  throw 'The game JAR changed during install validation. It was left untouched.'
}
if ($hasBackup -and (Get-FileSha256 $backupPath) -ne $initialBackupHash) {
  throw 'The original JAR backup changed during install validation. It was left untouched.'
}
if (-not $hasBackup) {
  if ($currentHash -ne $expectedBaseHash) {
    throw "This MewnBase JAR is not the supported original version. No backup or replacement was made. Expected SHA-256 $expectedBaseHash; found $currentHash."
  }
  Copy-Item -LiteralPath $jarPath -Destination $backupPath
  if ((Get-FileSha256 $backupPath) -ne $expectedBaseHash) {
    Remove-Item -LiteralPath $backupPath -Force
    throw 'The original JAR backup could not be verified; installation was stopped.'
  }
}

$tempJar = $jarPath + '.' + [guid]::NewGuid().ToString('N') + '.tmp'
$markerTemp = $markerPath + '.' + [guid]::NewGuid().ToString('N') + '.tmp'
$sourceArchive = $null
$targetArchive = $null
$targetStream = $null
$patchArchive = $null
try {
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
    gameVersion = [string]$package.Manifest.gameVersion
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
