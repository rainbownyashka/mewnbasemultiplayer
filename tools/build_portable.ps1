param(
  [string]$Name = "mewnbase_portable_win",
  [string]$OutputDir = "dist",
  [string]$RuntimeJar = ""
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path | Split-Path -Parent
Set-Location $root

$base = Join-Path $root "basegame"
$dist = Join-Path $root $OutputDir
$stamp = Get-Date -Format "yyyyMMdd_HHmmss"
$stageRoot = Join-Path $dist ($Name + "_" + $stamp)
$zipPath = $stageRoot + ".zip"

$includes = @(
  "config.json",
  "data",
  "game\desktop-1.0.jar",
  "jre",
  "MewnBase.exe",
  "README.md",
  "run.bat",
  "server.properties"
)

function Copy-Include($srcRelative) {
  $src = Join-Path $base $srcRelative
  if ($srcRelative -eq 'game\desktop-1.0.jar' -and $RuntimeJar) {
    $src = (Resolve-Path -LiteralPath $RuntimeJar).Path
  }
  $template = Join-Path $root ('tools\portable\' + $srcRelative)
  if (Test-Path -LiteralPath $template -PathType Leaf) { $src = $template }
  if (-not (Test-Path -LiteralPath $src)) {
    throw "Missing build input: $srcRelative"
  }

  $dst = Join-Path $stageRoot $srcRelative
  $dstParent = Split-Path -Parent $dst
  if ($dstParent) {
    New-Item -ItemType Directory -Force -Path $dstParent | Out-Null
  }

  if ((Get-Item -LiteralPath $src).PSIsContainer) {
    Copy-Item -LiteralPath $src -Destination $dst -Recurse -Force
  } else {
    Copy-Item -LiteralPath $src -Destination $dst -Force
  }
}

if ((Test-Path -LiteralPath $stageRoot) -or (Test-Path -LiteralPath $zipPath)) {
  throw 'Output already exists; choose a different Name or retry with a new timestamp.'
}

New-Item -ItemType Directory -Force -Path $dist | Out-Null
New-Item -ItemType Directory -Force -Path $stageRoot | Out-Null

foreach ($item in $includes) {
  Copy-Include $item
}

Compress-Archive -Path (Join-Path $stageRoot "*") -DestinationPath $zipPath -CompressionLevel Optimal

$zipInfo = Get-Item -LiteralPath $zipPath
Write-Host ("Portable zip: {0}" -f $zipPath)
Write-Host ("Bytes: {0}" -f $zipInfo.Length)
Write-Host "Included:"
foreach ($item in $includes) {
  Write-Host (" - {0}" -f $item)
}
