# Multiplayer Review Draft

This draft consolidates findings from old variants vs current src, focusing on multiplayer stability.

## Evidence Sources
- old/decompiledsafeedit/MULTIPLAYER.md
- old/_agent_client_server_review.txt
- old/_diff_summary.txt and old/_diff_list_*_vs_src.txt

## High-Risk Findings (Current src)
- Client uses non-threadsafe collections while reader/sender threads mutate shared state.
- Pending remote spawns are queued but never flushed (players may never appear).
- Protocol is mixed: binary preamble + INIT_DONE + READY expected, while older code assumed pure UTF. Any mismatch breaks connect.
- Server host position broadcast thread depends on `running=true` and may exit if started before `start()`.
- MultiplayerConfigMenu sets global flags even when connection fails; no socket read timeouts for initial blobs.
- World/Player network event handlers can mutate world off the main thread if not guarded.

## Candidate Fixes (Phase 1)
- Client: switch to concurrent collections, add `flushPendingSpawns()` on render thread.
- Client: optional UTF framing fallback (guarded behind diagnostic mode) if needed.
- Server: ensure host spawn/appearance is broadcast on connect and fix host-pos thread lifecycle.
- MultiplayerConfigMenu: add read timeouts and reset multiplayer flags on failure.
- World/Player: ensure network event application happens on the render thread (postRunnable) and suppress echo events.

## Build/Run Notes
- Java 21 is installed locally. For compatibility, use `--release 8` if compiling with javac.
- No full build system is present; recommend a batch compile of changed files with classpath = basegame jar.

## Next Steps
- Apply Phase 1 fixes with small, focused commits.
- After each change, run a targeted compile check on touched files.
- Validate connect flow: server starts, client connects, host and client spawn each other, positions update.
