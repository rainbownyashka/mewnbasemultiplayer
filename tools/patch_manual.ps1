param(
  [string[]]$Sources = @(
    "src\\com\\cairn4\\moonbase\\ui\\Hud.java",
    "src\\com\\cairn4\\moonbase\\Player.java",
    "src\\com\\cairn4\\moonbase\\Server.java",
    "src\\com\\cairn4\\moonbase\\World.java",
    "src\\com\\cairn4\\moonbase\\MoonBase.java"
  ),
  [int]$Kill = 1
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$proj = Resolve-Path (Join-Path $root '..')
Set-Location $proj

$jar = "basegame\\game\\desktop-1.0.jar"
$out = "out_patch"

if ($Kill -eq 1) {
  Get-CimInstance Win32_Process | Where-Object { $_.CommandLine -like '*desktop-1.0.jar*' } | ForEach-Object { try { Stop-Process -Id $_.ProcessId -Force } catch {} }
  Get-Process -Name MewnBase -ErrorAction SilentlyContinue | ForEach-Object { try { Stop-Process -Id $_.Id -Force } catch {} }
}

if (Test-Path $out) { Remove-Item -Recurse -Force $out }
New-Item -ItemType Directory -Path $out | Out-Null

$srcArgs = @()
foreach ($s in $Sources) {
  if (Test-Path $s) { $srcArgs += $s }
}
if ($srcArgs.Count -eq 0) { throw "No source files found to compile." }

javac --release 8 -cp $jar -sourcepath src\\com -d $out @srcArgs

$rootOut = (Resolve-Path $out).Path
$classes = Get-ChildItem -Path $out -Recurse -Filter "*.class"
foreach ($c in $classes) {
  $rel = $c.FullName.Substring($rootOut.Length + 1)
  jar uf $jar -C $out $rel
}

Write-Host "Patched jar: $jar"
Write-Host "Compiled: $($srcArgs -join ', ')"
