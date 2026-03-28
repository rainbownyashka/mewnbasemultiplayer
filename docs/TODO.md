# TODO

- Update compile pipeline to avoid version-mix (compile batch to out/, build jar from basegame + out/).
- Confirm Java version (8/11/17) and external jars for classpath.
- Review and port multiplayer diffs from old decompiledsafeedit to current src (see old/_diff_list_decompiledsafeedit_vs_src.txt).
- Phase 1 multiplayer fixes from docs/MP_DRAFT.md (thread safety, pending spawns, protocol, menu timeouts).
- Clean up duplicate archives in old/ if no longer needed.
- Multiplayer test checklist:
- Vehicle: driver control, passenger seat, VEH_META (drill/charge/wheels), trailer attach, generator fuel.
- Vehicle: enter/exit cycles (invisible player regression), nick reuse on reconnect.
- Inventory locks: vehicle trunk, storage crates, mining rigs (lock deny, unlock on close/disconnect).
- Base inventory sync: item move in storage/rig reflects on other client.
- Time/weather sync stability + HUD day spam check.
- Net spam off by default; diagclient on/off behavior.
- Eval server: run Java eval against live ctx.
- Player state persistence: verify reconnect restores inventory/stats/pos from multiplayer_players.json.
- Headless/server-only: verify `-Dmewnbase.serverOnly=1` boots into save + server, no UI, no RPC.
- Server-only mode still uses LWJGL; decide if/when to add true headless backend.
