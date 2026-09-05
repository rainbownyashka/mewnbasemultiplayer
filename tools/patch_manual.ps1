param(
  [string[]]$Sources = @(),
  [string]$SourceList = "",
  [int]$Kill = 1,
  [int]$UseChangedList = 1,
  [string]$JarPath = "basegame\\game\\desktop-1.0.jar"
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$proj = Resolve-Path (Join-Path $root '..')
Set-Location $proj

$jar = $JarPath
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
$merged = @($merged | Where-Object { $_ -and $_.Trim().Length -gt 0 } | ForEach-Object { $_.Trim() } | Sort-Object -Unique)

if ($UseChangedList -eq 1) {
  # Persist merged list for future runs
  try {
    $merged | ConvertTo-Json | Set-Content $changedFile -Encoding UTF8
  } catch {}
}

if ($merged.Count -eq 0) { throw "No source files found to compile." }

$javacArgs = @('--release', '8', '-cp', $jar, '-sourcepath', 'src\\com', '-d', $out) + $merged
& javac @javacArgs
if ($LASTEXITCODE -ne 0) { throw "javac failed with exit code $LASTEXITCODE" }

$rootOut = (Resolve-Path $out).Path
$classes = Get-ChildItem -Path $out -Recurse -Filter "*.class"
if ($classes.Count -eq 0) { throw "Compilation produced no classes." }
# One archive update, with an explicit failure check (PowerShell does not throw
# automatically when jar.exe fails, for example while the game holds the JAR).
& jar uf $jar -C $out .
if ($LASTEXITCODE -ne 0) { throw "jar update failed with exit code $LASTEXITCODE; runtime was not updated." }

Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [IO.Compression.ZipFile]::OpenRead((Resolve-Path $jar).Path)
try {
  foreach ($c in $classes) {
    $entryName = $c.FullName.Substring($rootOut.Length + 1).Replace('\', '/')
    $entry = $archive.GetEntry($entryName)
    if ($null -eq $entry) { throw "Missing class in runtime JAR: $entryName" }
    $stream = $entry.Open()
    $sha = [Security.Cryptography.SHA256]::Create()
    try { $actual = [BitConverter]::ToString($sha.ComputeHash($stream)).Replace('-', '') }
    finally { $stream.Dispose(); $sha.Dispose() }
    if ($actual -ne (Get-FileHash -LiteralPath $c.FullName -Algorithm SHA256).Hash) {
      throw "Runtime class differs from compiler output: $entryName"
    }
  }
} finally { $archive.Dispose() }
Write-Host "Verified $($classes.Count) class hashes in runtime JAR."

Write-Host "Patched jar: $jar"
Write-Host "Compiled: $($merged -join ', ')"
