# MewnBase Multiplayer installer

The Windows installer uses Inno Setup for the wizard and embeds only a versioned patch payload. It does not bundle the original game JAR, game data, launcher, runtime, or Java source files.

## Build

Build from a local, licensed MewnBase JAR and a patched runtime JAR:

```powershell
powershell -ExecutionPolicy Bypass -File tools\installer\Build-Installer.ps1 `
  -BaselineJar "C:\path\to\MewnBase\game\desktop-1.0.jar" `
  -PatchedJar "dist\desktop-v1.0.3.jar" `
  -PatchVersion "1.0.3"
```

The baseline JAR hash is embedded in the patch manifest. The installer only modifies an exact matching game JAR. It stops without replacing the JAR if the detected version differs.

Install Inno Setup 6 before building, or pass `-ISCCPath` to the script. The generated `MewnBaseMP-Setup-v*.exe` is the file to distribute; the adjacent patch ZIP is only a build input.

Inno Setup's publisher requests a paid license for commercial use of its compiler. This includes compiler use by CI in a commercial context.

For CI or a release rebuild, compile from the audited, versioned payload without access to a game JAR:

```powershell
powershell -ExecutionPolicy Bypass -File tools\installer\Build-Installer.ps1 `
  -PayloadZip "release-assets\MewnBaseMP-patch-v1.0.3.zip" `
  -PatchVersion "1.0.3" `
  -OutputDir "dist\mewnbasemultiplayer-installer-v1.0.3-release"
```

## Manual class archive

Create a second distribution option from the same verified payload. The resulting ZIP contains only changed `.class` files, keeping the `com/...` paths needed inside the JAR; it contains no manifest or installer files.

```powershell
powershell -ExecutionPolicy Bypass -File tools\installer\Build-ManualPatchZip.ps1 `
  -PayloadZip "release-assets\MewnBaseMP-patch-v1.0.3.zip" `
  -PatchVersion "1.0.3" `
  -OutputZip "dist\release-1.0.3\MewnBaseMP-Manual-Install-v1.0.3.zip"
```

The script verifies the payload manifest and every class hash, checks that all entries stay inside the approved game/console class packages, writes the class tree, reopens the output to verify it, and creates a `.sha256` sidecar. Release notes should tell players to close the game, verify the supported base JAR hash, copy `game\desktop-1.0.jar` to `game\desktop-1.0_original.jar`, then merge the ZIP's `com` tree into the JAR root with an archive manager. To restore, replace the patched JAR with the backup.

## Player install and restore

The setup searches Steam libraries using the Steam registry path and `libraryfolders.vdf`/`appmanifest_743130.acf`, checks common itch.io and game folders, and preselects a compatible install. If it finds none, choose the MewnBase root manually. The selected folder must contain `MewnBase.exe`, `data`, and `game\desktop-1.0.jar`.

Before changing the game, it copies the supported JAR to `game\desktop-1.0_original.jar`. Re-running the same setup offers a restore action. The backup is retained after restore. Saves and settings are not changed.

Steam updates can replace the patched JAR. In that case, verify the game files in Steam, install the patch again, and keep the original backup. A changed or unknown JAR is rejected rather than overwritten.
