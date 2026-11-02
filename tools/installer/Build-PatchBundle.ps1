[CmdletBinding()]
param(
  [Parameter(Mandatory = $true)]
  [string]$Payload101,

  [Parameter(Mandatory = $true)]
  [string]$Payload102,

  [Parameter(Mandatory = $true)]
  [string]$PatchVersion,

  [Parameter(Mandatory = $true)]
  [string]$OutputZip
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem

function Get-StreamSha256([IO.Stream]$Stream) {
  $sha = [Security.Cryptography.SHA256]::Create()
  try { return [BitConverter]::ToString($sha.ComputeHash($Stream)).Replace('-', '').ToLowerInvariant() }
  finally { $sha.Dispose() }
}

function Read-ValidatedPayload([string]$Path, [string]$ExpectedGameVersion, [string]$ExpectedPatchVersion) {
  $fullPath = (Resolve-Path -LiteralPath $Path).Path
  $archive = [IO.Compression.ZipFile]::OpenRead($fullPath)
  try {
    $names = New-Object 'System.Collections.Generic.HashSet[string]' ([StringComparer]::Ordinal)
    foreach ($entry in $archive.Entries) {
      if (-not $names.Add($entry.FullName)) { throw "Payload contains a duplicate path: $($entry.FullName)" }
    }
    $manifestEntry = $archive.GetEntry('patch-manifest.json')
    if ($null -eq $manifestEntry) { throw "Payload is missing patch-manifest.json: $fullPath" }
    $reader = [IO.StreamReader]::new($manifestEntry.Open(), [Text.Encoding]::UTF8, $true)
    try { $manifest = $reader.ReadToEnd() | ConvertFrom-Json }
    finally { $reader.Dispose() }

    $items = @($manifest.entries)
    if ($manifest.schemaVersion -ne 1 -or [string]$manifest.patchVersion -ne $ExpectedPatchVersion -or
        [string]$manifest.gameVersion -ne $ExpectedGameVersion -or
        [string]$manifest.baseJarSha256 -notmatch '^[0-9a-fA-F]{64}$' -or
        [string]$manifest.patchedRuntimeJarSha256 -notmatch '^[0-9a-fA-F]{64}$' -or
        [int]$manifest.entryCount -lt 1 -or [int]$manifest.entryCount -ne $items.Count -or
        $archive.Entries.Count -ne ($items.Count + 1)) {
      throw "Payload manifest does not match MewnBase $ExpectedGameVersion / patch ${ExpectedPatchVersion}: $fullPath"
    }

    $expected = New-Object 'System.Collections.Generic.Dictionary[string,string]' ([StringComparer]::Ordinal)
    foreach ($item in $items) {
      $name = [string]$item.path
      $hash = ([string]$item.sha256).ToLowerInvariant()
      if (-not $name.EndsWith('.class', [StringComparison]::Ordinal) -or $name.Contains('..') -or
          $name.StartsWith('/') -or $name.Contains('\') -or $hash -notmatch '^[0-9a-f]{64}$' -or
          $expected.ContainsKey($name)) {
        throw "Payload manifest has an unsafe or duplicate class entry: $name"
      }
      if (-not ($name.StartsWith('com/cairn4/moonbase/', [StringComparison]::Ordinal) -or
                $name.StartsWith('com/strongjoshua/console/', [StringComparison]::Ordinal))) {
        throw "Payload class is outside the approved packages: $name"
      }
      $expected.Add($name, $hash)
    }

    $actualNames = New-Object 'System.Collections.Generic.HashSet[string]' ([StringComparer]::Ordinal)
    foreach ($entry in $archive.Entries) {
      if ($entry.FullName -eq 'patch-manifest.json') { continue }
      if (-not $expected.ContainsKey($entry.FullName) -or -not $actualNames.Add($entry.FullName)) {
        throw "Payload has an unexpected or duplicate entry: $($entry.FullName)"
      }
      $stream = $entry.Open()
      try { $hash = Get-StreamSha256 $stream }
      finally { $stream.Dispose() }
      if ($hash -ne $expected[$entry.FullName]) { throw "Payload class hash mismatch: $($entry.FullName)" }
    }
    if ($actualNames.Count -ne $expected.Count) { throw "Payload is missing one or more classes: $fullPath" }

    return [pscustomobject]@{
      GameVersion = $ExpectedGameVersion
      PatchVersion = $ExpectedPatchVersion
      BaseJarSha256 = ([string]$manifest.baseJarSha256).ToLowerInvariant()
      PatchedRuntimeJarSha256 = ([string]$manifest.patchedRuntimeJarSha256).ToLowerInvariant()
      EntryCount = [int]$manifest.entryCount
      Path = $fullPath
      Sha256 = (Get-FileHash -LiteralPath $fullPath -Algorithm SHA256).Hash.ToLowerInvariant()
      Bytes = [IO.File]::ReadAllBytes($fullPath)
    }
  } finally { $archive.Dispose() }
}

$payloads = @(
  (Read-ValidatedPayload $Payload101 '1.0.1' $PatchVersion),
  (Read-ValidatedPayload $Payload102 '1.0.2' $PatchVersion)
)
if ($payloads[0].BaseJarSha256 -eq $payloads[1].BaseJarSha256) { throw 'Versioned payloads must target distinct original JAR hashes.' }

$outputPath = [IO.Path]::GetFullPath($OutputZip)
$outputParent = Split-Path -Parent $outputPath
if (-not (Test-Path -LiteralPath $outputParent -PathType Container)) { New-Item -ItemType Directory -Path $outputParent -Force | Out-Null }
if (Test-Path -LiteralPath $outputPath) { throw "Output already exists: $outputPath" }
$tempPath = $outputPath + '.' + [guid]::NewGuid().ToString('N') + '.tmp'
$outputStream = $null
$archive = $null
try {
  $variants = @()
  foreach ($payload in $payloads) {
    $payloadName = "payloads/MewnBaseMP-patch-v$PatchVersion-$($payload.GameVersion).zip"
    $variants += [ordered]@{
      gameVersion = $payload.GameVersion
      baseJarSha256 = $payload.BaseJarSha256
      patchedRuntimeJarSha256 = $payload.PatchedRuntimeJarSha256
      payloadPath = $payloadName
      payloadSha256 = $payload.Sha256
      entryCount = $payload.EntryCount
    }
  }
  $bundleManifest = [ordered]@{
    schemaVersion = 1
    patchVersion = $PatchVersion
    variants = $variants
  }
  $manifestBytes = [Text.UTF8Encoding]::new($false).GetBytes(($bundleManifest | ConvertTo-Json -Depth 6))

  $outputStream = [IO.File]::Open($tempPath, [IO.FileMode]::CreateNew, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None)
  $archive = [IO.Compression.ZipArchive]::new($outputStream, [IO.Compression.ZipArchiveMode]::Create, $false)
  $manifestZipEntry = $archive.CreateEntry('bundle-manifest.json', [IO.Compression.CompressionLevel]::Optimal)
  $manifestZipEntry.LastWriteTime = [DateTimeOffset]::new(1980, 1, 1, 0, 0, 0, [TimeSpan]::Zero)
  $manifestStream = $manifestZipEntry.Open()
  try { $manifestStream.Write($manifestBytes, 0, $manifestBytes.Length) }
  finally { $manifestStream.Dispose() }
  foreach ($index in 0..($payloads.Count - 1)) {
    $payload = $payloads[$index]
    $entryName = [string]$variants[$index].payloadPath
    $entry = $archive.CreateEntry($entryName, [IO.Compression.CompressionLevel]::NoCompression)
    $entry.LastWriteTime = [DateTimeOffset]::new(1980, 1, 1, 0, 0, 0, [TimeSpan]::Zero)
    $stream = $entry.Open()
    try { $stream.Write($payload.Bytes, 0, $payload.Bytes.Length) }
    finally { $stream.Dispose() }
  }
  $archive.Dispose(); $archive = $null
  $outputStream.Dispose(); $outputStream = $null

  $verify = [IO.Compression.ZipFile]::OpenRead($tempPath)
  try {
    if ($verify.Entries.Count -ne 3) { throw 'Bundle must contain one manifest and exactly two payloads.' }
    $verifyNames = New-Object 'System.Collections.Generic.HashSet[string]' ([StringComparer]::Ordinal)
    foreach ($entry in $verify.Entries) {
      if (-not $verifyNames.Add($entry.FullName)) { throw "Bundle contains a duplicate entry: $($entry.FullName)" }
    }
    if (-not $verifyNames.Contains('bundle-manifest.json')) { throw 'Bundle manifest was not written.' }
    for ($index = 0; $index -lt $payloads.Count; $index++) {
      $payload = $payloads[$index]
      $entry = $verify.GetEntry([string]$variants[$index].payloadPath)
      if ($null -eq $entry) { throw "Bundle is missing payload for MewnBase $($payload.GameVersion)." }
      $stream = $entry.Open()
      try { $hash = Get-StreamSha256 $stream }
      finally { $stream.Dispose() }
      if ($hash -ne $payload.Sha256) { throw "Bundled payload hash mismatch for MewnBase $($payload.GameVersion)." }
    }
  } finally { $verify.Dispose() }

  [IO.File]::Move($tempPath, $outputPath)
} finally {
  if ($null -ne $archive) { $archive.Dispose() }
  if ($null -ne $outputStream) { $outputStream.Dispose() }
  if (Test-Path -LiteralPath $tempPath) { Remove-Item -LiteralPath $tempPath -Force }
}

Write-Host "Versioned patch bundle: $outputPath"
Write-Host "MewnBase 1.0.1 baseline SHA-256: $($payloads[0].BaseJarSha256)"
Write-Host "MewnBase 1.0.2 baseline SHA-256: $($payloads[1].BaseJarSha256)"
Write-Host "Bundle SHA-256: $((Get-FileHash -LiteralPath $outputPath -Algorithm SHA256).Hash.ToLowerInvariant())"
