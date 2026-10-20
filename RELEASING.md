# Releasing

The repository contains the multiplayer project and patch tooling, but not the original MewnBase distribution. Releases should install the patch into the player's own licensed copy.

## Runtime changes

For a Java or runtime change, use the repository patch script with an explicit source list:

```powershell
powershell -ExecutionPolicy Bypass -File tools\patch_manual.ps1 `
  -SourceList "src\com\cairn4\moonbase\ui\MultiplayerConfigMenu.java,src\com\cairn4\moonbase\ui\HostServerSetupMenu.java" `
  -UseChangedList 0
```

The script compiles the selected files and updates `basegame\game\desktop-1.0.jar`. Verify the JAR changed and the build completed without compiler errors.

## Patch-only Windows installer

A tag such as `v1.0.3` makes GitHub Actions compile `MewnBaseMP-Setup-v1.0.3.exe` with Inno Setup and attach it with a SHA-256 file. The workflow does not attach a source ZIP or a portable game package. The installer embeds a class-only binary patch; it does not contain the original game JAR, data, launcher, or Java sources.

The matching patch input must be committed as `release-assets/MewnBaseMP-patch-v1.0.3.zip`. It contains the changed/new class files and a manifest with the expected original JAR hash. The workflow fails if the payload is missing or its version does not match the tag.

To prepare a new patch payload:

1. Build the patched runtime from a local licensed game copy and the explicit Java source list.
2. Run `tools\installer\Build-Installer.ps1` with the original game JAR, patched runtime JAR, and new patch version. Review the generated `MewnBaseMP-patch.zip`; it must contain only `.class` files plus `patch-manifest.json`.
3. Copy that payload to `release-assets\MewnBaseMP-patch-v<version>.zip` and commit it with the release changes.
4. Push a matching `v<version>` tag. CI builds and uploads the installer.

The installer detects Steam libraries and common itch.io/game folders. If no compatible game is found, the player must choose the root folder containing `MewnBase.exe`, `data`, and `game\desktop-1.0.jar`. Before changing the JAR, it saves a verified copy as `game\desktop-1.0_original.jar`; the restore action keeps this backup.

GitHub automatically provides source-code archives for tags in a public repository. This workflow no longer creates or attaches an extra `*-src.zip`, but it cannot hide the repository's automatic source archives. Hiding tracked sources requires changing repository visibility or moving them out of the public repository.

Inno Setup's publisher requests a paid license when the compiler is used commercially, including non-interactive CI use in a commercial context. Review its current terms before using this workflow for a commercial release.
