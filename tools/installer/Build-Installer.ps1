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
  function Assert-PatchArchive([IO.Compression.ZipArchive]$Archive, [string]$ExpectedGameVersion) {
    $names = New-Object 'System.Collections.Generic.HashSet[string]' ([StringComparer]::Ordinal)
    foreach ($entry in $Archive.Entries) {
      if (-not $names.Add($entry.FullName)) { throw "Patch archive contains a duplicate path: $($entry.FullName)" }
    }
    $manifestEntry = $Archive.GetEntry('patch-manifest.json')
    if ($null -eq $manifestEntry) { throw 'Patch payload is missing patch-manifest.json.' }
    $reader = New-Object IO.StreamReader($manifestEntry.Open(), [Text.Encoding]::UTF8, $true)
    try { $manifest = $reader.ReadToEnd() | ConvertFrom-Json }
    finally { $reader.Dispose() }
    $manifestItems = @($manifest.entries)
    if ($manifest.schemaVersion -ne 1 -or [string]$manifest.patchVersion -ne $PatchVersion -or
        [string]$manifest.baseJarSha256 -notmatch '^[0-9a-fA-F]{64}$' -or
        ($ExpectedGameVersion -and [string]$manifest.gameVersion -ne $ExpectedGameVersion) -or
        [int]$manifest.entryCount -lt 1 -or [int]$manifest.entryCount -ne $manifestItems.Count -or
        $manifest.entryCount -ne $Archive.Entries.Count - 1) {
      throw "Patch payload manifest does not match requested patch $PatchVersion / game $ExpectedGameVersion."
    }
    $expected = New-Object 'System.Collections.Generic.Dictionary[string,string]' ([StringComparer]::Ordinal)
    foreach ($item in $manifestItems) {
      $name = [string]$item.path
      $hash = ([string]$item.sha256).ToLowerInvariant()
      if (-not $name.EndsWith('.class', [StringComparison]::Ordinal) -or $name.Contains('..') -or
          $name.StartsWith('/') -or $name.Contains('\') -or $hash -notmatch '^[0-9a-f]{64}$' -or
          $expected.ContainsKey($name) -or
          -not ($name.StartsWith('com/cairn4/moonbase/', [StringComparison]::Ordinal) -or
                $name.StartsWith('com/strongjoshua/console/', [StringComparison]::Ordinal))) {
        throw "Patch payload manifest contains an invalid or duplicate class entry: $name"
      }
      $expected.Add($name, $hash)
    }
    $actual = New-Object 'System.Collections.Generic.HashSet[string]' ([StringComparer]::Ordinal)
    foreach ($entry in $Archive.Entries) {
      if ($entry.FullName -eq 'patch-manifest.json') { continue }
      if (-not $expected.ContainsKey($entry.FullName) -or -not $actual.Add($entry.FullName)) {
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
    return $manifest
  }

  $archive = [IO.Compression.ZipFile]::OpenRead($payloadPath)
  try {
    if ($null -ne $archive.GetEntry('patch-manifest.json')) {
      Assert-PatchArchive $archive '' | Out-Null
    } else {
      $bundleEntry = $archive.GetEntry('bundle-manifest.json')
      if ($null -eq $bundleEntry) { throw 'Payload ZIP is neither a class patch nor a versioned patch bundle.' }
      $reader = New-Object IO.StreamReader($bundleEntry.Open(), [Text.Encoding]::UTF8, $true)
      try { $bundle = $reader.ReadToEnd() | ConvertFrom-Json }
      finally { $reader.Dispose() }
      $variants = @($bundle.variants)
      if ($bundle.schemaVersion -ne 1 -or [string]$bundle.patchVersion -ne $PatchVersion -or $variants.Count -lt 2 -or
          $archive.Entries.Count -ne ($variants.Count + 1)) {
        throw "Patch bundle manifest does not match requested version $PatchVersion."
      }
      $variantVersions = New-Object 'System.Collections.Generic.HashSet[string]' ([StringComparer]::Ordinal)
      $variantHashes = New-Object 'System.Collections.Generic.HashSet[string]' ([StringComparer]::OrdinalIgnoreCase)
      $variantPaths = New-Object 'System.Collections.Generic.HashSet[string]' ([StringComparer]::Ordinal)
      foreach ($variant in $variants) {
        $gameVersion = [string]$variant.gameVersion
        $baseHash = ([string]$variant.baseJarSha256).ToLowerInvariant()
        $payloadName = [string]$variant.payloadPath
        $payloadHash = ([string]$variant.payloadSha256).ToLowerInvariant()
        if ($gameVersion -notmatch '^\d+\.\d+\.\d+$' -or -not $variantVersions.Add($gameVersion) -or
            $baseHash -notmatch '^[0-9a-f]{64}$' -or -not $variantHashes.Add($baseHash) -or
            $payloadHash -notmatch '^[0-9a-f]{64}$' -or
            -not $payloadName.StartsWith('payloads/', [StringComparison]::Ordinal) -or
            -not $payloadName.EndsWith('.zip', [StringComparison]::Ordinal) -or $payloadName.Contains('..') -or
            $payloadName.Contains('\') -or -not $variantPaths.Add($payloadName)) {
          throw "Patch bundle contains an invalid or duplicate variant for game $gameVersion."
        }
        $nestedEntry = $archive.GetEntry($payloadName)
        if ($null -eq $nestedEntry) { throw "Patch bundle is missing its payload: $payloadName" }
        $nestedStream = $nestedEntry.Open()
        $memory = New-Object IO.MemoryStream
        try {
          $nestedStream.CopyTo($memory)
          $nestedBytes = $memory.ToArray()
          $payloadSha = [Security.Cryptography.SHA256]::Create()
          try { $actualPayloadHash = [BitConverter]::ToString($payloadSha.ComputeHash($nestedBytes)).Replace('-', '').ToLowerInvariant() }
          finally { $payloadSha.Dispose() }
          if ($actualPayloadHash -ne $payloadHash) { throw "Bundled payload hash mismatch: $payloadName" }
          $nestedMemory = New-Object IO.MemoryStream(, $nestedBytes)
          try {
            $nestedArchive = New-Object IO.Compression.ZipArchive($nestedMemory, [IO.Compression.ZipArchiveMode]::Read, $false)
            try {
              $nestedManifest = Assert-PatchArchive $nestedArchive $gameVersion
              if ([string]$nestedManifest.baseJarSha256 -ne $baseHash -or
                  [int]$nestedManifest.entryCount -ne [int]$variant.entryCount -or
                  ([string]$nestedManifest.patchedRuntimeJarSha256).ToLowerInvariant() -ne ([string]$variant.patchedRuntimeJarSha256).ToLowerInvariant()) {
                throw "Bundle metadata does not match nested payload: $payloadName"
              }
            } finally { $nestedArchive.Dispose() }
          } finally { $nestedMemory.Dispose() }
        } finally { $nestedStream.Dispose(); $memory.Dispose() }
      }
      foreach ($entry in $archive.Entries) {
        if ($entry.FullName -ne 'bundle-manifest.json' -and -not $variantPaths.Contains($entry.FullName)) {
          throw "Patch bundle contains an unexpected file: $($entry.FullName)"
        }
      }
      if (-not $variantVersions.Contains('1.0.1') -or -not $variantVersions.Contains('1.0.2')) {
        throw 'The v1.0.4 installer bundle must include both MewnBase 1.0.1 and 1.0.2.'
      }
    }
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
