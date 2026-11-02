# MewnBase Multiplayer installer

The Windows installer uses Inno Setup and embeds a versioned, class-only patch bundle. It does not bundle the original game JAR, game data, launcher, runtime, or Java sources. The wizard supports Russian and English.

## Audited game versions

The v1.0.4 bundle accepts these exact clean JAR hashes:

| MewnBase | Clean JAR SHA-256 | Payload |
| --- | --- | --- |
| 1.0.1 | `9452d5588c457995599a3fb95ef6d570fa4aa2f3d367bd0dbe2d63a303272b6c` | `MewnBaseMP-patch-v1.0.4-1.0.1.zip` |
| 1.0.2 | `18d88af1c48f475d25c45a1bcf5b127071f48d771958798e5fad62e14baee517` | `MewnBaseMP-patch-v1.0.4-1.0.2.zip` |

The installer selects the payload by original JAR SHA-256. It creates and verifies `game\desktop-1.0_original.jar` before applying the patch. Re-running the installer offers restore, and the backup remains after restoration. Unknown or modified JARs are rejected.

## Build

Install Inno Setup 6.7.3, or pass its `ISCC.exe` path. Build both runtime variants from audited original JARs using the tracked explicit source list:

```powershell
powershell -ExecutionPolicy Bypass -File tools\installer\Build-VersionedPatchSet.ps1 `
  -Baseline101 "C:\path\to\MewnBase-1.0.1\game\desktop-1.0.jar" `
  -Baseline102 "C:\path\to\MewnBase-1.0.2\game\desktop-1.0.jar" `
  -OutputDir "dist\mewnbasemultiplayer-patch-v1.0.4-build" `
  -PatchVersion "1.0.4" `
  -WorkspaceJar "basegame\game\desktop-1.0.jar"
```

This compiles Java 8-compatible class files, verifies each merged JAR by reading the class hashes back, and creates the separate payloads and the installer bundle. Copy the three ZIPs to `release-assets\` with the generated names, then build the installer:

```powershell
powershell -ExecutionPolicy Bypass -File tools\installer\Build-Installer.ps1 `
  -PayloadZip "release-assets\MewnBaseMP-patch-v1.0.4.zip" `
  -PatchVersion "1.0.4" `
  -OutputDir "dist\release-1.0.4"
```

`Build-Installer.ps1` validates both nested payloads and their hashes before calling Inno Setup. Distribute the resulting `MewnBaseMP-Setup-v1.0.4.exe`; the bundle ZIP is a build input.

Inno Setup's publisher requests a paid license for commercial use of its compiler, including CI use in a commercial context.

## Manual class archives

The manual option contains changed `.class` files under their `com/...` JAR paths, without a manifest, installer, source files, or game files. Build one archive per game version:

```powershell
powershell -ExecutionPolicy Bypass -File tools\installer\Build-ManualPatchZip.ps1 `
  -PayloadZip "release-assets\MewnBaseMP-patch-v1.0.4-1.0.2.zip" `
  -PatchVersion "1.0.4" `
  -OutputZip "dist\release-1.0.4\MewnBaseMP-Manual-Install-v1.0.4-MewnBase-1.0.2.zip"
```

The script verifies every class against the payload manifest, rejects paths outside the game and console packages, reopens the output to verify it, and writes a `.sha256` sidecar. Before merging the ZIP into the JAR, close the game, verify the matching clean JAR hash, and copy it to `game\desktop-1.0_original.jar`. Merge the ZIP's `com` folder into the JAR root with an archive manager. Restore by replacing the patched JAR with the backup.

## Install detection

The setup searches Steam libraries using the Steam registry path and `libraryfolders.vdf`/`appmanifest_743130.acf`, then checks common itch.io and game folders. If it finds none, select the root containing `MewnBase.exe`, `data`, and `game\desktop-1.0.jar`. Steam updates can replace the patch; verify the game files and reinstall while keeping the original backup.
