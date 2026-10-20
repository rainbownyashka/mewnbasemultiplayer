[CmdletBinding()]
param(
  [string]$BaselineJar = '',
  [string]$PatchedJar = '',
  [string]$PayloadZip = '',
  [string]$PatchVersion = '1.0.3',
  [string]$OutputDir = '',
  [string]$ISCCPath = ''
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot | Split-Path -Parent
Set-Location $root

if (-not $OutputDir) {
  $OutputDir = Join-Path $root ("dist\mewnbasemultiplayer-installer-v{0}" -f $PatchVersion)
} elseif (-not [IO.Path]::IsPathRooted($OutputDir)) {
  $OutputDir = Join-Path $root $OutputDir
}
$OutputDir = [IO.Path]::GetFullPath($OutputDir)

if (-not $ISCCPath) {
  $command = Get-Command ISCC.exe -ErrorAction SilentlyContinue
  if ($command) { $ISCCPath = $command.Source }
}
if (-not $ISCCPath) {
  $compilerCandidates = @(
    (Join-Path ${env:ProgramFiles(x86)} 'Inno Setup 6\ISCC.exe'),
    (Join-Path $env:ProgramFiles 'Inno Setup 6\ISCC.exe'),
    (Join-Path ${env:ProgramFiles(x86)} 'Inno Setup 7\ISCC.exe'),
    (Join-Path $env:ProgramFiles 'Inno Setup 7\ISCC.exe'),
    (Join-Path $env:LOCALAPPDATA 'Programs\Inno Setup 6\ISCC.exe'),
    (Join-Path $env:LOCALAPPDATA 'Programs\Inno Setup 7\ISCC.exe')
  )
  $ISCCPath = $compilerCandidates | Where-Object { Test-Path -LiteralPath $_ -PathType Leaf } | Select-Object -First 1
}
if (-not $ISCCPath -or -not (Test-Path -LiteralPath $ISCCPath -PathType Leaf)) {
  throw 'Inno Setup compiler ISCC.exe was not found. Install Inno Setup 6, or pass -ISCCPath explicitly.'
}

if (Test-Path -LiteralPath $OutputDir) {
  throw "Output folder already exists; choose another path: $OutputDir"
}
New-Item -ItemType Directory -Path $OutputDir | Out-Null

$resolvedPayloadZip = ''
if ($PayloadZip) {
  $payloadPath = if ([IO.Path]::IsPathRooted($PayloadZip)) { $PayloadZip } else { Join-Path $root $PayloadZip }
  $payloadPath = (Resolve-Path -LiteralPath $payloadPath).Path
  Add-Type -AssemblyName System.IO.Compression
  Add-Type -AssemblyName System.IO.Compression.FileSystem
  $archive = [IO.Compression.ZipFile]::OpenRead($payloadPath)
  try {
    $manifestEntry = $archive.GetEntry('patch-manifest.json')
    if ($null -eq $manifestEntry) { throw 'Patch payload is missing patch-manifest.json.' }
    $reader = New-Object IO.StreamReader($manifestEntry.Open(), [Text.Encoding]::UTF8, $true)
    try { $manifest = $reader.ReadToEnd() | ConvertFrom-Json }
    finally { $reader.Dispose() }
    if ($manifest.schemaVersion -ne 1 -or [string]$manifest.patchVersion -ne $PatchVersion -or
        [string]$manifest.baseJarSha256 -notmatch '^[0-9a-fA-F]{64}$' -or
        [int]$manifest.entryCount -ne @($manifest.entries).Count -or
        [int]$manifest.entryCount -ne $archive.Entries.Count - 1) {
      throw "Patch payload manifest does not match requested version $PatchVersion."
    }
    $expected = New-Object 'System.Collections.Generic.Dictionary[string,string]' ([StringComparer]::Ordinal)
    foreach ($item in $manifest.entries) {
      $name = [string]$item.path
      $hash = ([string]$item.sha256).ToLowerInvariant()
      if (-not $name.EndsWith('.class', [StringComparison]::Ordinal) -or $name.Contains('..') -or
          $name.StartsWith('/') -or $name.Contains('\') -or $hash -notmatch '^[0-9a-f]{64}$' -or
          $expected.ContainsKey($name)) {
        throw "Patch payload manifest contains an invalid or duplicate class entry: $name"
      }
      $expected.Add($name, $hash)
    }
    $actual = New-Object 'System.Collections.Generic.HashSet[string]' ([StringComparer]::Ordinal)
    foreach ($entry in $archive.Entries) {
      if ($entry.FullName -eq 'patch-manifest.json') { continue }
      if (-not $entry.FullName.EndsWith('.class', [StringComparison]::Ordinal) -or
          -not $expected.ContainsKey($entry.FullName) -or -not $actual.Add($entry.FullName)) {
        throw "Patch payload contains an unexpected or duplicate file: $($entry.FullName)"
      }
      $stream = $entry.Open()
      $sha = [Security.Cryptography.SHA256]::Create()
      try { $actualHash = [BitConverter]::ToString($sha.ComputeHash($stream)).Replace('-', '').ToLowerInvariant() }
      finally { $sha.Dispose(); $stream.Dispose() }
      if ($actualHash -ne $expected[$entry.FullName]) {
        throw "Patch payload class failed SHA-256 verification: $($entry.FullName)"
      }
    }
    if ($actual.Count -ne $expected.Count) { throw 'Patch payload is missing one or more manifest classes.' }
  } finally { $archive.Dispose() }
  $resolvedPayloadZip = $payloadPath
} else {
  if (-not $BaselineJar -or -not $PatchedJar) {
    throw 'Provide -PayloadZip, or provide both -BaselineJar and -PatchedJar.'
  }
  $resolvedPayloadZip = Join-Path $OutputDir 'MewnBaseMP-patch.zip'
}
$installerPath = Join-Path $OutputDir ("MewnBaseMP-Setup-v{0}.exe" -f $PatchVersion)
$builder = Join-Path $PSScriptRoot 'Build-PatchPayload.ps1'
$setupScript = Join-Path $PSScriptRoot 'MewnBaseMP-Setup.iss'

if (-not $PayloadZip) {
  & $builder -BaselineJar $BaselineJar -PatchedJar $PatchedJar -OutputZip $resolvedPayloadZip -PatchVersion $PatchVersion
}

Push-Location $PSScriptRoot
try {
  & $ISCCPath "/DSetupVersion=$PatchVersion" "/DPayloadZip=$resolvedPayloadZip" "/DOutputDir=$OutputDir" $setupScript
  if ($LASTEXITCODE -ne 0) { throw "Inno Setup compiler failed with exit code $LASTEXITCODE" }
} finally {
  Pop-Location
}

if (-not (Test-Path -LiteralPath $installerPath -PathType Leaf)) {
  throw "Inno Setup reported success but did not create the installer: $installerPath"
}
$installerInfo = Get-Item -LiteralPath $installerPath
$installerHash = (Get-FileHash -LiteralPath $installerPath -Algorithm SHA256).Hash
Write-Host "Installer: $installerPath"
Write-Host "Bytes: $($installerInfo.Length)"
Write-Host "SHA-256: $installerHash"
Write-Host 'Distribute the .exe installer. The patch ZIP is a build input and is not needed beside it.'
