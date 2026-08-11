# Releasing

This repo contains *sources + patch tooling*, not the proprietary base game files.

## Targeted Java/UI Changes

For a Java or runtime change, use the repository patch script with an explicit source list:

```powershell
powershell -ExecutionPolicy Bypass -File tools\patch_manual.ps1 `
  -SourceList "src\com\cairn4\moonbase\ui\MultiplayerConfigMenu.java,src\com\cairn4\moonbase\ui\HostServerSetupMenu.java" `
  -UseChangedList 0
```

The script compiles the selected files and updates `basegame\game\desktop-1.0.jar`. After it finishes, verify that the JAR timestamp/size changed and that there were no compiler errors. Use `-UseChangedList 0` for focused fixes so stale files from previous runs are not compiled accidentally.

## CI Releases (GitHub Actions)

When you push a tag like `v1.0.0`, CI creates a GitHub Release and uploads:
- `mewnbasemultiplayer-<tag>-src.zip` (tracked files only)

If you also want CI to upload a prebuilt portable package, you can commit it to:
- `release-assets/*.zip`

CI will copy any `release-assets/*.zip` into the release.

## Portable Build (Manual)

The portable package cannot be built in CI unless you provide the base game jar + JRE.

Locally, use the generated clean build folder and zip it:
- `dist/mewnbase_clean_*_portable_slim/` → zip and upload as release asset
