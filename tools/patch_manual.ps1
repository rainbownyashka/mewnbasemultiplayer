param(
  [string[]]$Sources = @(),
  [string]$SourceList = "",
  [int]$Kill = 1,
  [int]$UseChangedList = 1
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$proj = Resolve-Path (Join-Path $root '..')
Set-Location $proj

$jar = "basegame\\game\\desktop-1.0.jar"
$out = "out_patch"
$changedFile = "autocompile.changed.json"

if ($Kill -eq 1) {
  Get-CimInstance Win32_Process | Where-Object { $_.CommandLine -like '*desktop-1.0.jar*' } | ForEach-Object { try { Stop-Process -Id $_.ProcessId -Force } catch {} }
  Get-Process -Name MewnBase -ErrorAction SilentlyContinue | ForEach-Object { try { Stop-Process -Id $_.Id -Force } catch {} }
}

if (Test-Path $out) { Remove-Item -Recurse -Force $out }
New-Item -ItemType Directory -Path $out | Out-Null

$srcArgs = @()
if ($SourceList -ne "") {
  $Sources = $SourceList -split ","
}
foreach ($s in $Sources) {
  $s = $s.Trim('"').Trim()
  if (Test-Path $s) { $srcArgs += (Resolve-Path $s).Path }
}

# Merge with persistent changed list (compile all files that were ever changed)
$changedList = @()
if ($UseChangedList -eq 1 -and (Test-Path $changedFile)) {
  try {
    $changedList = Get-Content $changedFile | ConvertFrom-Json
  } catch {
    $changedList = @()
  }
}

$merged = @()
$merged += $changedList
$merged += $srcArgs
$merged = $merged | Where-Object { $_ -and $_.Trim().Length -gt 0 } | ForEach-Object { $_.Trim() } | Sort-Object -Unique

if ($UseChangedList -eq 1) {
  # Persist merged list for future runs
  try {
    $merged | ConvertTo-Json | Set-Content $changedFile -Encoding UTF8
  } catch {}
}

if ($merged.Count -eq 0) { throw "No source files found to compile." }

javac --release 8 -cp $jar -sourcepath src\\com -d $out @merged

$rootOut = (Resolve-Path $out).Path
$classes = Get-ChildItem -Path $out -Recurse -Filter "*.class"
foreach ($c in $classes) {
  $rel = $c.FullName.Substring($rootOut.Length + 1)
  jar uf $jar -C $out $rel
}

Write-Host "Patched jar: $jar"
Write-Host "Compiled: $($merged -join ', ')"
