# Assets: Adding/Updating Tile Textures

This project uses LibGDX texture atlases. Ground tiles and many world sprites come from `Tiles/tiles.atlas` + `Tiles/tiles.png` inside `basegame/game/desktop-1.0.jar`.

## Quick paths

- Working editable files:
  - `assets_override/Tiles/tiles.png`
  - `assets_override/Tiles/tiles.atlas`
- Optional modded atlas:
  - `assets_override/Tiles/modded.png`
  - `assets_override/Tiles/modded.atlas`
- Source extraction scratch:
  - `work/atlas_extract/Tiles/tiles.png`
  - `work/atlas_extract/Tiles/tiles.atlas`

## Important rules

- The atlas header `size: W,H` must match the PNG size exactly.
- The atlas and PNG must be stored in the jar under the **correct path**:
  - `Tiles/tiles.atlas`
  - `Tiles/tiles.png`
  - `Tiles/modded.atlas` (if used)
  - `Tiles/modded.png` (if used)
- Do not rely on tint alone for color shifts; to fully change hue, update the sprite pixels.
- If you add new regions (e.g., `test/ice-*`), you must add them to the atlas and ensure the atlas is loaded into the skin.

## Editing existing tiles (no atlas layout changes)

1. Edit the PNG in place:
   - `assets_override/Tiles/tiles.png`
2. Repack into jar:
   ```powershell
   jar uf basegame\game\desktop-1.0.jar -C assets_override Tiles\tiles.png
   ```

## Adding new tile regions

1. Update `assets_override/Tiles/tiles.png` (or create a new `modded.png`).
2. Update the atlas:
   - `assets_override/Tiles/tiles.atlas` (or `modded.atlas`)
   - Ensure the `size:` header matches the PNG size.
3. Repack into jar under **Tiles/** path:
   ```powershell
   jar uf basegame\game\desktop-1.0.jar -C assets_override Tiles\tiles.atlas -C assets_override Tiles\tiles.png
   ```
   For a dedicated modded atlas:
   ```powershell
   jar uf basegame\game\desktop-1.0.jar -C assets_override Tiles\modded.atlas -C assets_override Tiles\modded.png
   ```
4. Ensure the atlas is loaded in code:
   - `src/com/cairn4/moonbase/MoonBase.java` must `load("Tiles/modded.atlas", TextureAtlas.class)` if you use a new atlas.
   - `src/com/cairn4/moonbase/ui/GameScreen.java` must `skin.addRegions(moddedAtlas)` to register the regions.
5. Compile + patch jar if code changed:
   ```powershell
   powershell -ExecutionPolicy Bypass -File tools\patch_manual.ps1 -SourceList "src\com\cairn4\moonbase\MoonBase.java,src\com\cairn4\moonbase\ui\GameScreen.java"
   ```

## Common errors and fixes

- `No Drawable ... modded/ice-15-alt3`
  - Region name exists in atlas, but the atlas is not loaded into the skin or not inside jar under `Tiles/`.

- `Couldn't load file: Tiles/modded.png`
  - The atlas wasn’t loaded or the jar path is wrong. Make sure `Tiles/modded.atlas` and `Tiles/modded.png` are present **inside the jar**.

- Regions exist but still not found
  - Check `tiles.atlas` header `size:` and the actual PNG size.
  - Verify that the regions are in the atlas file used by the game (extract from jar and check).

## Verify what is inside the jar

```powershell
jar tf basegame\game\desktop-1.0.jar | Select-String -Pattern "Tiles/tiles|Tiles/modded"
```

## Notes

- `basegame/` is ignored in git, so keep edited assets in `assets_override/` for version control.
- When testing new tiles, use a new world or new planet to avoid cached ground tiles.
