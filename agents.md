# Agent Rules

- Before claiming a Java/runtime change is done, compile the affected files and update `basegame/game/desktop-1.0.jar`.
- After patching, verify the jar was actually updated and the build completed without compiler errors.
- Treat multiplayer, launcher, and registry changes as high-risk: never assume source edits are enough.
- If a change touches runtime behavior, include the verification step in the same turn.
- Do not leave documentation saying a fix is done if the jar has not been rebuilt.
- For targeted fixes, prefer an explicit source list and clean build inputs; do not rely on accumulated changed-file state unless the broader rebuild is intentional.
