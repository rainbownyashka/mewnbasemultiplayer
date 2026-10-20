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

## Player install and restore

The setup searches Steam libraries using the Steam registry path and `libraryfolders.vdf`/`appmanifest_743130.acf`, checks common itch.io and game folders, and preselects a compatible install. If it finds none, choose the MewnBase root manually. The selected folder must contain `MewnBase.exe`, `data`, and `game\desktop-1.0.jar`.

Before changing the game, it copies the supported JAR to `game\desktop-1.0_original.jar`. Re-running the same setup offers a restore action. The backup is retained after restore. Saves and settings are not changed.

Steam updates can replace the patched JAR. In that case, verify the game files in Steam, install the patch again, and keep the original backup. A changed or unknown JAR is rejected rather than overwritten.
