# Releasing

The repository contains the multiplayer project and patch tooling, but not the original MewnBase distribution. Releases patch the player's own licensed game installation.

## Supported game JARs

The v1.0.4 installer and manual archives support these exact clean JARs:

| MewnBase | Original JAR SHA-256 | Patch archive |
| --- | --- | --- |
| 1.0.1 | `9452d5588c457995599a3fb95ef6d570fa4aa2f3d367bd0dbe2d63a303272b6c` | `MewnBaseMP-patch-v1.0.4-1.0.1.zip` |
| 1.0.2 | `18d88af1c48f475d25c45a1bcf5b127071f48d771958798e5fad62e14baee517` | `MewnBaseMP-patch-v1.0.4-1.0.2.zip` |

The 1.0.2 build keeps its own `DesktopAdapter`, launcher/data classes, pinned-item HUD contract, proxy-tile behavior, chunk filtering, and save-folder API. `Patch-MewnBaseJar.ps1` chooses a payload only after matching the original JAR hash. It saves that JAR as `game\desktop-1.0_original.jar` and keeps the backup after restore.

## Build the versioned payloads

Use the explicit source list and the two audited baseline JARs:

```powershell
powershell -ExecutionPolicy Bypass -File tools\installer\Build-VersionedPatchSet.ps1 `
  -Baseline101 "C:\path\to\MewnBase-1.0.1\game\desktop-1.0.jar" `
  -Baseline102 "C:\path\to\MewnBase-1.0.2\game\desktop-1.0.jar" `
  -OutputDir "dist\mewnbasemultiplayer-patch-v1.0.4-build" `
  -PatchVersion "1.0.4" `
  -WorkspaceJar "basegame\game\desktop-1.0.jar"
```

The script compiles Java 8 bytecode from `tools\installer\patch-sources.txt`, rejects baselines with different hashes, produces separate 1.0.1 and 1.0.2 class payloads, creates the version-selecting bundle, verifies class read-back, and updates the local workspace JAR with the 1.0.1 variant. Check both `javac-*.log` files and the final JAR hash before describing the build as complete.

Copy the three generated ZIPs to `release-assets\` using the versioned names above, then build the installer:

```powershell
powershell -ExecutionPolicy Bypass -File tools\installer\Build-Installer.ps1 `
  -PayloadZip "release-assets\MewnBaseMP-patch-v1.0.4.zip" `
  -PatchVersion "1.0.4" `
  -OutputDir "dist\release-1.0.4"
```

Build the two manual archives from their matching payloads with `Build-ManualPatchZip.ps1`. They contain only changed `.class` files and keep each `com/...` path for merging into the JAR. Fixed ZIP entry timestamps make the manual archive hashes reproducible. The versioned bundle is a build input, not a player download.

The GitHub workflow expects a matching `v<version>` tag and `docs\releases\v<version>.md`. It builds the installer and manual archives, then attaches SHA-256 sidecars and the bilingual release notes. The installer wizard supports English and Russian, searches Steam libraries and common itch.io/game folders, and asks the player to select the root folder if no compatible game is found.

GitHub automatically provides source-code archives for public tags. This workflow does not create a separate source ZIP; it cannot hide GitHub's automatic tag archives.

Inno Setup's publisher requests a paid license when its compiler is used commercially, including non-interactive CI use in a commercial context. Follow the publisher's current terms for the intended release use.
