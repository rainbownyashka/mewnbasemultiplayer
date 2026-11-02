[CmdletBinding()]
param(
  [Parameter(Mandatory = $true)]
  [string]$Baseline101,

  [Parameter(Mandatory = $true)]
  [string]$Baseline102,

  [Parameter(Mandatory = $true)]
  [string]$OutputDir,

  [string]$PatchVersion = '1.0.4',

  [string]$WorkspaceJar = ''
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem
$root = Split-Path -Parent $PSScriptRoot | Split-Path -Parent
Set-Location $root

$supported = @{
  '1.0.1' = '9452d5588c457995599a3fb95ef6d570fa4aa2f3d367bd0dbe2d63a303272b6c'
  '1.0.2' = '18d88af1c48f475d25c45a1bcf5b127071f48d771958798e5fad62e14baee517'
}
$baselinePaths = @{
  '1.0.1' = (Resolve-Path -LiteralPath $Baseline101).Path
  '1.0.2' = (Resolve-Path -LiteralPath $Baseline102).Path
}
foreach ($version in @('1.0.1', '1.0.2')) {
  $actualHash = (Get-FileHash -LiteralPath $baselinePaths[$version] -Algorithm SHA256).Hash.ToLowerInvariant()
  if ($actualHash -ne $supported[$version]) {
    throw "The supplied MewnBase $version JAR is not the audited baseline. Expected $($supported[$version]); found $actualHash."
  }
}

if (-not [IO.Path]::IsPathRooted($OutputDir)) { $OutputDir = Join-Path $root $OutputDir }
$OutputDir = [IO.Path]::GetFullPath($OutputDir)
if (Test-Path -LiteralPath $OutputDir) { throw "Output directory already exists: $OutputDir" }
New-Item -ItemType Directory -Path $OutputDir | Out-Null

$sourceListPath = Join-Path $root 'tools\installer\patch-sources.txt'
$sourceRelativePaths = @(Get-Content -LiteralPath $sourceListPath | ForEach-Object { $_.Trim() } | Where-Object { $_ })
if ($sourceRelativePaths.Count -lt 1) { throw 'The explicit patch source list is empty.' }
foreach ($relative in $sourceRelativePaths) {
  if ($relative -match '(^|[\\/])\.\.([\\/]|$)' -or -not $relative.StartsWith('com/')) {
    throw "Unsafe or unsupported source-list entry: $relative"
  }
  $resolvedSource = Join-Path $root ('src\' + $relative.Replace('/', '\'))
  if (-not (Test-Path -LiteralPath $resolvedSource -PathType Leaf)) { throw "Listed source file is missing: $resolvedSource" }
}

$javac = (Get-Command javac.exe -ErrorAction Stop).Source
$builder = Join-Path $PSScriptRoot 'Build-PatchPayload.ps1'
$workDir = Join-Path $OutputDir ('.work-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $workDir | Out-Null

function Set-ExactReplacement([string]$Path, [string]$OldText, [string]$NewText, [int]$ExpectedCount) {
  $content = [IO.File]::ReadAllText($Path)
  $count = [regex]::Matches($content, [regex]::Escape($OldText)).Count
  if ($count -ne $ExpectedCount) {
    throw "Version transform expected $ExpectedCount occurrence(s) of '$OldText' in $Path, found $count."
  }
  [IO.File]::WriteAllText($Path, $content.Replace($OldText, $NewText), (New-Object Text.UTF8Encoding($false)))
}

function Merge-ClassDirectoryIntoJar([string]$BaseJar, [string]$ClassDirectory, [string]$OutputJar) {
  if (Test-Path -LiteralPath $OutputJar) { Remove-Item -LiteralPath $OutputJar -Force }
  $classFiles = @(Get-ChildItem -LiteralPath $ClassDirectory -Recurse -File -Filter '*.class' | Sort-Object FullName)
  $classNames = New-Object 'System.Collections.Generic.HashSet[string]' ([StringComparer]::Ordinal)
  foreach ($classFile in $classFiles) {
    $entryName = $classFile.FullName.Substring($ClassDirectory.Length + 1).Replace('\', '/')
    if (-not $classNames.Add($entryName)) { throw "Compiler emitted a duplicate class path: $entryName" }
  }

  $sourceArchive = [IO.Compression.ZipFile]::OpenRead($BaseJar)
  $outputStream = $null
  $outputArchive = $null
  try {
    $outputStream = [IO.File]::Open($OutputJar, [IO.FileMode]::CreateNew, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None)
    $outputArchive = [IO.Compression.ZipArchive]::new($outputStream, [IO.Compression.ZipArchiveMode]::Create, $false)
    foreach ($entry in $sourceArchive.Entries) {
      if ($classNames.Contains($entry.FullName)) { continue }
      $copy = $outputArchive.CreateEntry($entry.FullName, [IO.Compression.CompressionLevel]::Optimal)
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
    foreach ($classFile in $classFiles) {
      $entryName = $classFile.FullName.Substring($ClassDirectory.Length + 1).Replace('\', '/')
      $entry = $outputArchive.CreateEntry($entryName, [IO.Compression.CompressionLevel]::Optimal)
      $entry.LastWriteTime = [DateTimeOffset]::new(1980, 1, 1, 0, 0, 0, [TimeSpan]::Zero)
      $input = [IO.File]::OpenRead($classFile.FullName)
      $output = $entry.Open()
      try { $input.CopyTo($output) }
      finally { $output.Dispose(); $input.Dispose() }
    }
    $outputArchive.Dispose(); $outputArchive = $null
    $outputStream.Dispose(); $outputStream = $null
  } finally {
    if ($null -ne $outputArchive) { $outputArchive.Dispose() }
    if ($null -ne $outputStream) { $outputStream.Dispose() }
    $sourceArchive.Dispose()
  }

  $verification = [IO.Compression.ZipFile]::OpenRead($OutputJar)
  try {
    $counts = New-Object 'System.Collections.Generic.Dictionary[string,int]' ([StringComparer]::Ordinal)
    foreach ($entry in $verification.Entries) {
      if (-not $classNames.Contains($entry.FullName)) { continue }
      if ($counts.ContainsKey($entry.FullName)) { $counts[$entry.FullName]++ } else { $counts.Add($entry.FullName, 1) }
      $stream = $entry.Open()
      $sha = [Security.Cryptography.SHA256]::Create()
      try { $entryHash = [BitConverter]::ToString($sha.ComputeHash($stream)).Replace('-', '').ToLowerInvariant() }
      finally { $sha.Dispose(); $stream.Dispose() }
      $classFile = $classFiles | Where-Object { $_.FullName.Substring($ClassDirectory.Length + 1).Replace('\', '/') -eq $entry.FullName } | Select-Object -First 1
      if ($null -eq $classFile -or $entryHash -ne (Get-FileHash -LiteralPath $classFile.FullName -Algorithm SHA256).Hash.ToLowerInvariant()) {
        throw "Patched runtime class failed read-back verification: $($entry.FullName)"
      }
    }
    if ($counts.Count -ne $classNames.Count) { throw 'Patched runtime JAR is missing compiled classes.' }
    foreach ($name in $classNames) {
      if (-not $counts.ContainsKey($name) -or $counts[$name] -ne 1) { throw "Patched runtime has a duplicate or missing class entry: $name" }
    }
    if ($null -eq $verification.GetEntry('META-INF/MANIFEST.MF')) { throw 'Patched runtime JAR has no manifest.' }
  } finally { $verification.Dispose() }
}

function Invoke-VariantBuild([string]$GameVersion, [string[]]$RelativeSources) {
  $variantRoot = Join-Path $workDir $GameVersion
  $variantSources = Join-Path $variantRoot 'src'
  $classes = Join-Path $variantRoot 'classes'
  New-Item -ItemType Directory -Path $variantSources,$classes -Force | Out-Null

  foreach ($relative in $RelativeSources) {
    $sourcePath = Join-Path $root ('src\' + $relative.Replace('/', '\'))
    $targetPath = Join-Path $variantSources $relative.Replace('/', '\')
    $targetParent = Split-Path -Parent $targetPath
    New-Item -ItemType Directory -Path $targetParent -Force | Out-Null
    Copy-Item -LiteralPath $sourcePath -Destination $targetPath
  }

  if ($GameVersion -eq '1.0.2') {
    $protoBase = Join-Path $variantSources 'com\cairn4\moonbase\tiles\ProtoBase.java'
    Set-ExactReplacement $protoBase 'addProxyTileImage(pt.x, pt.y);' "addProxyTileImage(pt.x, pt.y);`r`n            addProxyTile(pt.x, pt.y);" 1

    $moonBase = Join-Path $variantSources 'com\cairn4\moonbase\MoonBase.java'
    Set-ExactReplacement $moonBase 'public static final String VERSION = "1.0.1";' 'public static final String VERSION = "1.0.2";' 1
    Set-ExactReplacement $moonBase 'public static final String BUILD_DATE = "11/19/2023";' 'public static final String BUILD_DATE = "11/8/2025";' 1
    Set-ExactReplacement $moonBase 'public static final String BUILD_DATE_SHORT = "Nov 2023";' 'public static final String BUILD_DATE_SHORT = "Nov 2025";' 1

    $mainMenu = Join-Path $variantSources 'com\cairn4\moonbase\ui\MainMenu.java'
    Set-ExactReplacement $mainMenu 'v1.0.1' 'v1.0.2' 1
    Set-ExactReplacement $mainMenu 'Nov 2023' 'Nov 2025' 1

    $gameScreen = Join-Path $variantSources 'com\cairn4\moonbase\ui\GameScreen.java'
    Set-ExactReplacement $gameScreen 'Build version 1.0.1' 'Build version 1.0.2' 1

    $launcher = Join-Path $variantSources 'com\cairn4\moonbase\desktop\DesktopLauncher.java'
    Set-ExactReplacement $launcher '1.0.1' '1.0.2' 3
  }

  $sourcePaths = @($RelativeSources | ForEach-Object { Join-Path $variantSources $_.Replace('/', '\') })
  $responsePath = Join-Path $variantRoot 'javac-sources.args'
  $responseLines = @($sourcePaths | ForEach-Object { '"' + $_.Replace('\', '\\').Replace('"', '\"') + '"' })
  [IO.File]::WriteAllLines($responsePath, $responseLines, [Text.Encoding]::ASCII)

  $compileLog = Join-Path $OutputDir ("javac-$GameVersion.log")
  $compilerOutput = & $javac --release 8 '-cp' $baselinePaths[$GameVersion] '-sourcepath' '' '-d' $classes ("@" + $responsePath) 2>&1
  $compilerExit = $LASTEXITCODE
  [IO.File]::WriteAllText($compileLog, (($compilerOutput | Out-String) + "`r`nexitCode=$compilerExit`r`n"), [Text.Encoding]::UTF8)
  if ($compilerExit -ne 0) { throw "Java 8 compilation for MewnBase $GameVersion failed; see $compileLog." }

  $classFiles = @(Get-ChildItem -LiteralPath $classes -Recurse -File -Filter '*.class')
  if ($classFiles.Count -lt 1) { throw "Compilation emitted no class files for MewnBase $GameVersion." }
  foreach ($classFile in $classFiles) {
    $relativeClass = $classFile.FullName.Substring($classes.Length + 1)
    if ($relativeClass -notmatch '^(com[\\/](cairn4[\\/]moonbase|strongjoshua[\\/]console)[\\/].+\.class)$') {
      throw "Compiler emitted a class outside the approved patch packages: $relativeClass"
    }
  }

  $runtimeJar = Join-Path $variantRoot ("desktop-$GameVersion-patched.jar")
  Merge-ClassDirectoryIntoJar $baselinePaths[$GameVersion] $classes $runtimeJar

  $classpath = $classes + ';' + $baselinePaths[$GameVersion]
  $moonBaseApi = & (Join-Path (Split-Path -Parent $javac) 'javap.exe') '-classpath' $classpath '-p' 'com.cairn4.moonbase.MoonBase' 2>&1 | Out-String
  if ($moonBaseApi -notmatch 'public boolean isSteamActive\(\);') {
    throw "MewnBase $GameVersion build does not contain MoonBase.isSteamActive()."
  }
  $platformApi = & (Join-Path (Split-Path -Parent $javac) 'javap.exe') '-classpath' $classpath '-p' 'com.cairn4.moonbase.PlatformAdapter' 2>&1 | Out-String
  if ($platformApi -notmatch 'public abstract boolean isSteamActive\(\);') {
    throw "MewnBase $GameVersion build does not contain PlatformAdapter.isSteamActive()."
  }

  $payload = Join-Path $OutputDir ("MewnBaseMP-patch-v$PatchVersion-$GameVersion.zip")
  & $builder -BaselineJar $baselinePaths[$GameVersion] -PatchedJar $runtimeJar -OutputZip $payload -PatchVersion $PatchVersion -GameVersion $GameVersion
  if ($LASTEXITCODE -ne 0) { throw "Patch payload creation failed for MewnBase $GameVersion." }
  Write-Host "MewnBase ${GameVersion}: compiled $($RelativeSources.Count) explicit sources into $($classFiles.Count) classes."
}

try {
  $sources101 = @($sourceRelativePaths)
  $sources102 = @($sourceRelativePaths | Where-Object { $_ -ne 'com/cairn4/moonbase/desktop/DesktopAdapter.java' })
  if ($sources102.Count -ne ($sourceRelativePaths.Count - 1)) {
    throw 'The 1.0.2 build must preserve its baseline DesktopAdapter.class and exclude exactly its 1.0.1 source.'
  }

  Invoke-VariantBuild '1.0.1' $sources101
  Invoke-VariantBuild '1.0.2' $sources102

  $bundleBuilder = Join-Path $PSScriptRoot 'Build-PatchBundle.ps1'
  $bundle = Join-Path $OutputDir "MewnBaseMP-patch-v$PatchVersion.zip"
  & $bundleBuilder -Payload101 (Join-Path $OutputDir "MewnBaseMP-patch-v$PatchVersion-1.0.1.zip") -Payload102 (Join-Path $OutputDir "MewnBaseMP-patch-v$PatchVersion-1.0.2.zip") -PatchVersion $PatchVersion -OutputZip $bundle
  if ($LASTEXITCODE -ne 0) { throw 'Versioned patch bundle creation failed.' }

  if ($WorkspaceJar) {
    $workspacePath = if ([IO.Path]::IsPathRooted($WorkspaceJar)) { $WorkspaceJar } else { Join-Path $root $WorkspaceJar }
    $workspacePath = (Resolve-Path -LiteralPath $workspacePath).Path
    $classSource = Join-Path $workDir '1.0.1\classes'
    $stagedWorkspace = Join-Path $workDir 'workspace-updated.jar'
    Copy-Item -LiteralPath $workspacePath -Destination $stagedWorkspace
    Merge-ClassDirectoryIntoJar $workspacePath $classSource $stagedWorkspace
    Copy-Item -LiteralPath $stagedWorkspace -Destination $workspacePath -Force
    Write-Host "Updated workspace JAR: $workspacePath"
    Write-Host "Workspace JAR SHA-256: $((Get-FileHash -LiteralPath $workspacePath -Algorithm SHA256).Hash.ToLowerInvariant())"
  }
} finally {
  if (Test-Path -LiteralPath $workDir) { Remove-Item -LiteralPath $workDir -Recurse -Force }
}

Write-Host "Versioned patch set ready: $OutputDir"
