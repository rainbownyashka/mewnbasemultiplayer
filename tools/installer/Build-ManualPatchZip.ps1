[CmdletBinding()]
param(
  [Parameter(Mandatory = $true)]
  [string]$PayloadZip,

  [Parameter(Mandatory = $true)]
  [string]$PatchVersion,

  [Parameter(Mandatory = $true)]
  [string]$OutputZip
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem

$payloadPath = (Resolve-Path -LiteralPath $PayloadZip).Path
$outputPath = [IO.Path]::GetFullPath($OutputZip)
$outputParent = Split-Path -Parent $outputPath
$hashPath = $outputPath + '.sha256'
if (-not (Test-Path -LiteralPath $outputParent -PathType Container)) {
  New-Item -ItemType Directory -Path $outputParent -Force | Out-Null
}
if (Test-Path -LiteralPath $outputPath -PathType Leaf) { throw "Output already exists: $outputPath" }
if (Test-Path -LiteralPath $hashPath -PathType Leaf) { throw "SHA-256 file already exists: $hashPath" }

$allowedPrefixes = @('com/cairn4/moonbase/', 'com/strongjoshua/console/')
$temporaryZip = $outputPath + '.' + [guid]::NewGuid().ToString('N') + '.tmp'
$temporaryHash = $hashPath + '.' + [guid]::NewGuid().ToString('N') + '.tmp'
$sourceArchive = $null
$outputStream = $null
$outputArchive = $null

function Get-StreamSha256([IO.Stream]$Stream) {
  $sha = [Security.Cryptography.SHA256]::Create()
  try {
    return [BitConverter]::ToString($sha.ComputeHash($Stream)).Replace('-', '').ToLowerInvariant()
  } finally {
    $sha.Dispose()
  }
}

function Test-SafeClassPath([string]$Path) {
  if (-not $Path.EndsWith('.class', [StringComparison]::Ordinal) -or
      $Path.StartsWith('/', [StringComparison]::Ordinal) -or
      $Path.Contains('\') -or $Path.Contains(':')) { return $false }
  foreach ($segment in $Path.Split('/')) {
    if ([string]::IsNullOrEmpty($segment) -or $segment -eq '.' -or $segment -eq '..') { return $false }
  }
  foreach ($prefix in $allowedPrefixes) {
    if ($Path.StartsWith($prefix, [StringComparison]::Ordinal)) { return $true }
  }
  return $false
}

try {
  $sourceArchive = [IO.Compression.ZipFile]::OpenRead($payloadPath)
  $manifestEntry = $sourceArchive.GetEntry('patch-manifest.json')
  if ($null -eq $manifestEntry) { throw 'Patch payload is missing patch-manifest.json.' }
  $manifestReader = [IO.StreamReader]::new($manifestEntry.Open(), [Text.Encoding]::UTF8, $true)
  try { $manifest = $manifestReader.ReadToEnd() | ConvertFrom-Json }
  finally { $manifestReader.Dispose() }

  $manifestItems = @($manifest.entries)
  if ($manifest.schemaVersion -ne 1 -or [string]$manifest.patchVersion -ne $PatchVersion -or
      [string]$manifest.baseJarSha256 -notmatch '^[0-9a-fA-F]{64}$' -or
      [int]$manifest.entryCount -ne $manifestItems.Count -or
      [int]$manifest.entryCount -ne ($sourceArchive.Entries.Count - 1)) {
    throw "Patch payload manifest does not match version $PatchVersion or its archive entries."
  }

  $expected = New-Object 'System.Collections.Generic.Dictionary[string,string]' ([StringComparer]::Ordinal)
  foreach ($item in $manifestItems) {
    $name = [string]$item.path
    $hash = ([string]$item.sha256).ToLowerInvariant()
    if (-not (Test-SafeClassPath $name) -or $hash -notmatch '^[0-9a-f]{64}$' -or $expected.ContainsKey($name)) {
      throw "Patch manifest contains an invalid or duplicate class entry: $name"
    }
    $expected.Add($name, $hash)
  }

  $actualNames = New-Object 'System.Collections.Generic.HashSet[string]' ([StringComparer]::Ordinal)
  $outputStream = [IO.File]::Open($temporaryZip, [IO.FileMode]::CreateNew, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None)
  $outputArchive = [IO.Compression.ZipArchive]::new($outputStream, [IO.Compression.ZipArchiveMode]::Create, $false)

  foreach ($entry in $sourceArchive.Entries) {
    if ($entry.FullName -eq 'patch-manifest.json') { continue }
    if (-not (Test-SafeClassPath $entry.FullName) -or
        -not $expected.ContainsKey($entry.FullName) -or -not $actualNames.Add($entry.FullName)) {
      throw "Patch payload contains an unexpected or duplicate file: $($entry.FullName)"
    }

    $inputStream = $entry.Open()
    try { $actualHash = Get-StreamSha256 $inputStream }
    finally { $inputStream.Dispose() }
    if ($actualHash -ne $expected[$entry.FullName]) {
      throw "Patch payload class failed SHA-256 verification: $($entry.FullName)"
    }

    $outputEntry = $outputArchive.CreateEntry($entry.FullName, [IO.Compression.CompressionLevel]::Optimal)
    $inputStream = $entry.Open()
    $outputEntryStream = $outputEntry.Open()
    try { $inputStream.CopyTo($outputEntryStream) }
    finally { $outputEntryStream.Dispose(); $inputStream.Dispose() }
  }
  if ($actualNames.Count -ne $expected.Count) { throw 'Patch payload is missing one or more manifest classes.' }

  $outputArchive.Dispose(); $outputArchive = $null
  $outputStream.Dispose(); $outputStream = $null
  $sourceArchive.Dispose(); $sourceArchive = $null

  $verifyArchive = [IO.Compression.ZipFile]::OpenRead($temporaryZip)
  try {
    if ($verifyArchive.Entries.Count -ne $expected.Count) { throw 'Manual archive contains an unexpected number of files.' }
    $verifiedNames = New-Object 'System.Collections.Generic.HashSet[string]' ([StringComparer]::Ordinal)
    foreach ($entry in $verifyArchive.Entries) {
      if (-not (Test-SafeClassPath $entry.FullName) -or
          -not $expected.ContainsKey($entry.FullName) -or -not $verifiedNames.Add($entry.FullName)) {
        throw "Manual archive contains an unexpected or duplicate file: $($entry.FullName)"
      }
      $inputStream = $entry.Open()
      try { $actualHash = Get-StreamSha256 $inputStream }
      finally { $inputStream.Dispose() }
      if ($actualHash -ne $expected[$entry.FullName]) {
        throw "Manual archive class failed SHA-256 verification: $($entry.FullName)"
      }
    }
  } finally { $verifyArchive.Dispose() }

  $zipHash = (Get-FileHash -LiteralPath $temporaryZip -Algorithm SHA256).Hash.ToLowerInvariant()
  [IO.File]::WriteAllText($temporaryHash, "$zipHash  $(Split-Path -Leaf $outputPath)$([Environment]::NewLine)", [Text.Encoding]::ASCII)
  [IO.File]::Move($temporaryZip, $outputPath)
  [IO.File]::Move($temporaryHash, $hashPath)

  Write-Host "Manual install archive: $outputPath"
  Write-Host "Class files: $($expected.Count)"
  Write-Host "Baseline JAR SHA-256: $($manifest.baseJarSha256)"
  Write-Host "Archive SHA-256: $zipHash"
} finally {
  if ($null -ne $outputArchive) { $outputArchive.Dispose() }
  if ($null -ne $outputStream) { $outputStream.Dispose() }
  if ($null -ne $sourceArchive) { $sourceArchive.Dispose() }
  if (Test-Path -LiteralPath $temporaryZip) { Remove-Item -LiteralPath $temporaryZip -Force }
  if (Test-Path -LiteralPath $temporaryHash) { Remove-Item -LiteralPath $temporaryHash -Force }
}
