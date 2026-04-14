# Releasing

This repo contains *sources + patch tooling*, not the proprietary base game files.

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

