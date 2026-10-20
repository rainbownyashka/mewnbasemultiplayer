[CmdletBinding()]
param(
  [Parameter(Mandatory = $true)]
  [string]$BaselineJar,

  [Parameter(Mandatory = $true)]
  [string]$PatchedJar,

  [Parameter(Mandatory = $true)]
  [string]$OutputZip,

  [Parameter(Mandatory = $true)]
  [string]$PatchVersion
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem

$baselinePath = (Resolve-Path -LiteralPath $BaselineJar).Path
$patchedPath = (Resolve-Path -LiteralPath $PatchedJar).Path
$outputPath = [IO.Path]::GetFullPath($OutputZip)
$outputParent = Split-Path -Parent $outputPath
if (-not (Test-Path -LiteralPath $outputParent -PathType Container)) {
  New-Item -ItemType Directory -Path $outputParent -Force | Out-Null
}
if (Test-Path -LiteralPath $outputPath) {
  throw "Output already exists: $outputPath"
}

$allowedPrefixes = @('com/cairn4/moonbase/', 'com/strongjoshua/console/')
$excludedEntries = @('META-INF/versions/9/module-info.class')
$stagePath = Join-Path $outputParent ('.mewnbase-patch-' + [guid]::NewGuid().ToString('N'))
$temporaryZip = $outputPath + '.' + [guid]::NewGuid().ToString('N') + '.tmp'
$baseArchive = $null
$patchedArchive = $null
$outputArchive = $null
$outputStream = $null

function Get-StreamSha256([IO.Stream]$Stream) {
  $sha = [Security.Cryptography.SHA256]::Create()
  try {
    return [BitConverter]::ToString($sha.ComputeHash($Stream)).Replace('-', '').ToLowerInvariant()
  } finally {
    $sha.Dispose()
  }
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

try {
  $baseArchive = [IO.Compression.ZipFile]::OpenRead($baselinePath)
  $patchedArchive = [IO.Compression.ZipFile]::OpenRead($patchedPath)

  $baseHashes = New-Object 'System.Collections.Generic.Dictionary[string,System.Collections.Generic.List[string]]' ([StringComparer]::Ordinal)
  foreach ($entry in $baseArchive.Entries) {
    if (-not $entry.FullName.EndsWith('.class', [StringComparison]::Ordinal)) { continue }
    $entryStream = $entry.Open()
    try { $hash = Get-StreamSha256 $entryStream }
    finally { $entryStream.Dispose() }
    if (-not $baseHashes.ContainsKey($entry.FullName)) {
      $baseHashes.Add($entry.FullName, (New-Object 'System.Collections.Generic.List[string]'))
    }
    $baseHashes[$entry.FullName].Add($hash)
  }

  $patchedNames = New-Object 'System.Collections.Generic.HashSet[string]' ([StringComparer]::Ordinal)
  $patchEntries = New-Object 'System.Collections.Generic.List[object]'
  foreach ($entry in $patchedArchive.Entries) {
    if (-not $entry.FullName.EndsWith('.class', [StringComparison]::Ordinal)) { continue }
    if (-not $patchedNames.Add($entry.FullName)) {
      throw "Patched runtime has duplicate class entry: $($entry.FullName)"
    }
    if ($excludedEntries -contains $entry.FullName) { continue }

    $entryStream = $entry.Open()
    try { $patchedHash = Get-StreamSha256 $entryStream }
    finally { $entryStream.Dispose() }

    $changed = -not $baseHashes.ContainsKey($entry.FullName)
    if (-not $changed) {
      foreach ($baseHash in $baseHashes[$entry.FullName]) {
        if ($baseHash -ne $patchedHash) { $changed = $true; break }
      }
    }
    if (-not $changed) { continue }

    $allowed = $false
    foreach ($prefix in $allowedPrefixes) {
      if ($entry.FullName.StartsWith($prefix, [StringComparison]::Ordinal)) {
        $allowed = $true
        break
      }
    }
    if (-not $allowed) {
      throw "Changed class is outside the approved game/mod packages: $($entry.FullName)"
    }

    $patchEntries.Add([pscustomobject]@{
      Path = $entry.FullName
      Sha256 = $patchedHash
      Bytes = (Get-EntryBytes $entry)
    })
  }

  foreach ($entryName in $baseHashes.Keys) {
    if ($entryName -in $excludedEntries) { continue }
    if (-not $patchedNames.Contains($entryName) -and
        ($entryName.StartsWith($allowedPrefixes[0], [StringComparison]::Ordinal) -or
         $entryName.StartsWith($allowedPrefixes[1], [StringComparison]::Ordinal))) {
      throw "Patched runtime removed an existing game class; deletion patches are not supported: $entryName"
    }
  }
  if ($patchEntries.Count -eq 0) { throw 'No changed class files were found; refusing to build an empty installer payload.' }

  $baseSha256 = (Get-FileHash -LiteralPath $baselinePath -Algorithm SHA256).Hash.ToLowerInvariant()
  $patchedSha256 = (Get-FileHash -LiteralPath $patchedPath -Algorithm SHA256).Hash.ToLowerInvariant()
  New-Item -ItemType Directory -Path $stagePath | Out-Null

  foreach ($entry in $patchEntries) {
    if ($entry.Path.Contains('..') -or $entry.Path.StartsWith('/') -or $entry.Path.Contains('\')) {
      throw "Unsafe JAR entry path: $($entry.Path)"
    }
    $localPath = Join-Path $stagePath ($entry.Path.Replace('/', [IO.Path]::DirectorySeparatorChar))
    $localParent = Split-Path -Parent $localPath
    New-Item -ItemType Directory -Path $localParent -Force | Out-Null
    [IO.File]::WriteAllBytes($localPath, $entry.Bytes)
  }

  $manifest = [ordered]@{
    schemaVersion = 1
    patchVersion = $PatchVersion
    baseJarSha256 = $baseSha256
    patchedRuntimeJarSha256 = $patchedSha256
    entryCount = $patchEntries.Count
    entries = @($patchEntries | Sort-Object Path | ForEach-Object {
      [ordered]@{ path = $_.Path; sha256 = $_.Sha256 }
    })
  }
  $manifestPath = Join-Path $stagePath 'patch-manifest.json'
  $manifestJson = $manifest | ConvertTo-Json -Depth 5
  [IO.File]::WriteAllText($manifestPath, $manifestJson, (New-Object Text.UTF8Encoding($false)))

  $outputStream = [IO.File]::Open($temporaryZip, [IO.FileMode]::CreateNew, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None)
  $outputArchive = New-Object IO.Compression.ZipArchive($outputStream, [IO.Compression.ZipArchiveMode]::Create, $false)
  foreach ($file in Get-ChildItem -LiteralPath $stagePath -Recurse -File | Sort-Object FullName) {
    $relative = $file.FullName.Substring($stagePath.Length + 1).Replace('\', '/')
    $zipEntry = $outputArchive.CreateEntry($relative, [IO.Compression.CompressionLevel]::Optimal)
    $source = [IO.File]::OpenRead($file.FullName)
    $destination = $zipEntry.Open()
    try { $source.CopyTo($destination) }
    finally { $destination.Dispose(); $source.Dispose() }
  }
  $outputArchive.Dispose(); $outputArchive = $null
  $outputStream.Dispose(); $outputStream = $null

  $verifyArchive = [IO.Compression.ZipFile]::OpenRead($temporaryZip)
  try {
    $names = New-Object 'System.Collections.Generic.HashSet[string]' ([StringComparer]::Ordinal)
    foreach ($entry in $verifyArchive.Entries) {
      if (-not $names.Add($entry.FullName)) { throw "Payload archive contains a duplicate path: $($entry.FullName)" }
    }
    if (-not $names.Contains('patch-manifest.json') -or $names.Count -ne ($patchEntries.Count + 1)) {
      throw 'Payload archive does not match the generated patch manifest.'
    }
  } finally { $verifyArchive.Dispose() }

  Move-Item -LiteralPath $temporaryZip -Destination $outputPath
  Write-Host "Payload: $outputPath"
  Write-Host "Baseline SHA-256: $baseSha256"
  Write-Host "Patched runtime SHA-256: $patchedSha256"
  Write-Host "Class entries: $($patchEntries.Count)"
} finally {
  if ($null -ne $outputArchive) { $outputArchive.Dispose() }
  if ($null -ne $outputStream) { $outputStream.Dispose() }
  if ($null -ne $patchedArchive) { $patchedArchive.Dispose() }
  if ($null -ne $baseArchive) { $baseArchive.Dispose() }
  if (Test-Path -LiteralPath $temporaryZip) { Remove-Item -LiteralPath $temporaryZip -Force }
  if (Test-Path -LiteralPath $stagePath) { Remove-Item -LiteralPath $stagePath -Recurse -Force }
}
