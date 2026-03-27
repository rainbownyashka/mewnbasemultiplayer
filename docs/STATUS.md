# Status

Last update: 2026-03-27

Changes
- Restructured workspace to a simpler layout (basegame/, src/, work/, out/, tools/, backups/, old/).
- Updated autocompile script paths to new layout and moved it under tools/.
- Root run script now calls tools/mewnbase_autoupdate.py.
- Generated a diff list of multiplayer-related changes between old decompiled sources and current src (12 files).
- Audited all old decompiled variants vs current src and captured counts in old/_diff_summary.txt.
- Added multiplayer review draft in docs/MP_DRAFT.md.
- Client: made multiplayer state thread-safe and added pending-spawn flush to prevent remote players getting stuck.
- MultiplayerConfigMenu: added socket read timeout and rollback of multiplayer flags on failed connect.
- Server: keep host position broadcaster alive even if GameScreen is attached before server starts.
- Basegame: restored `basegame/data` folder by moving core JSON/PNG data files into it so runtime lookup works.
- Basegame: moved `locale/` under `data/locale/` to match runtime lookup path `data/locale/mewnbase`.
- Basegame: structure check recorded in docs/STRUCTURE_CHECK.txt (no issues found).
- Added optional runtime eval server (disabled by default, enable with -Dmewnbase.eval=1).
- Added optional UI test server (disabled by default, enable with -Dmewnbase.uitest=1) to list/click buttons safely.
- UiTestServer: added menu-aware commands (MENU_FOCUS, MENU_STACK, MENU_LIST, CLICK_MENU) for custom menu structure.
- Added basegame/run_log.bat to persist game logs to basegame/logs/.
- MainMenu: auto-opens Multiplayer menu when new MP properties are present.
- MultiplayerConfigMenu: added MP auto-fill/auto-connect via JVM properties (client-only, no server auto-start).
- Hud: added defensive debug-label update guard when player/world not yet initialized (prevents client NPE on join).
- MoonBase: legacy `-Dmewnbase.connect` now routes through menu-based auto-connect instead of instant-run.
- MainMenu: auto-open multiplayer now runs after intro animation completes.
- MultiplayerConfigMenu: patched jar with updated inner classes to avoid NoSuchMethodError.
- DesktopLauncher: avoid crashlog NPE when currentSaveFolder is null.
- Added tools/mewnbase_autoupdate_ai.py: AI-only autocompile with changed-file tracking, work/ refresh, and pre-kill.
- tools/mewnbase_autoupdate_ai.py now also kills cmd.exe instances running run_*.bat scripts.
- basegame/run_client.bat updated to pass mp nick and autoconnect.
- MultiplayerConfigMenu: trigger auto-connect scheduling at construction time (BaseScreen.showMenu does not call show()).
- Hud: guard mission observer attach when current mission is null during multiplayer join.
- Added basegame/fulltest.bat to run 40s server+client test and emit log paths.
- Added FileLogger and -Dmewnbase.logfile to force flushed logs when stdout is redirected.
- fulltest.bat now passes -Dmewnbase.logfile for both server and client.
- GameScreen: guard updateRichPresence when mission/dayCycle not yet initialized during MP join.
- MultiplayerConfigMenu: on successful sync fetch, use vanilla LoadingScreen pipeline instead of direct GameScreen.
- Server: send INIT_DONE marker after binary init so client doesn't consume SPAWNREMOTE as init marker.
- MultiplayerConfigMenu: removed custom sync socket; now uses vanilla LoadingScreen + Client.connect blob fetch.
- World: defer multiplayer_received load until worldData.json exists (prevents early load error).
- World: guard start-state update when lander is null (prevents client crash during deferred load).
- fulltest.bat now uses absolute game jar path so it works when run from basegame/.
- Server: defer client broadcast registration until after INIT_DONE + READY handshake to avoid binary/text interleaving.
- Hud: re-activate once multiplayer player becomes available so HUD groups (health/oxygen/icons) are restored.
- Hud: only show the interact white-square cursor when controller mode is enabled (prevents stuck cursor for mouse users).
- fulltest.bat: default run has no time limit; pass seconds to auto-stop (e.g., `fulltest.bat 40`).
- Added basegame/fulltestnonai.bat (manual console run with pause).
- Added tools/patch_manual.ps1 for manual compile+patch.
- BuildingPlacementCursor: default hidden when no build item equipped (prevents white square on client).
- PlayerAnimController: broadcast walk anim/flip when server is active (host movement now visible to clients).

Impact
- Original game files remain in basegame/ and should not be edited in place.
- Scripts now assume src/ for sources, out/ for compiled classes, work/ for packaging.

Verification
- Compiled MultiplayerConfigMenu.java and Server.java with javac --release 8 (classpath basegame jar, sourcepath src/com).
- Compiled MoonBase.java, debug/RuntimeEval.java, and debug/UiTestServer.java with javac --release 8.
- Compiled updated UiTestServer.java with javac --release 8 and patched jar.
- Compiled MainMenu.java and MultiplayerConfigMenu.java with javac --release 8 and patched jar.
- Compiled Hud.java with javac --release 8 and patched jar.
- Added basegame/fulltest.bat (not executed yet).
- Compiled FileLogger.java and MoonBase.java with javac --release 8 and patched jar.
- Compiled GameScreen.java with javac --release 8 and patched jar.
- Compiled MultiplayerConfigMenu.java with javac --release 8 and patched jar (including inner classes).
- Compiled Server.java with javac --release 8 and patched jar.
- Compiled MultiplayerConfigMenu.java with javac --release 8 and patched jar (including inner classes).
- Compiled MoonBase.java and MainMenu.java with javac --release 8 and patched jar.
- Compiled MultiplayerConfigMenu.java with javac --release 8 and patched jar (including inner classes).
- Compiled Hud.java with javac --release 8 and patched jar.
- Compiled DesktopLauncher.java with javac --release 8 and patched jar.
- Updated tools/mewnbase_autoupdate_ai.py (not executed yet).
- Updated basegame/run_client.bat (not executed yet).
- Compiled MultiplayerConfigMenu.java with javac --release 8 and patched jar (including inner classes).
- UI test via UiTestServer: MainMenu -> Multiplayer -> Create Server (7777) -> GameScreen.
- Compiled World.java with javac --release 8 and patched jar.
- Ran basegame/fulltest.bat (2026-03-27) and confirmed client reached deferred load and remained stable (no crash in logs).
- Compiled Server.java with javac --release 8 (including all Server$*.class) and patched jar.
- Ran basegame/fulltest.bat (2026-03-27) and verified no INIT_DONE/read errors in client log.
- Ran basegame/fulltest.bat (2026-03-27); no client/server errors in logs (HUD visibility to be confirmed visually).
- Compiled Hud.java with javac --release 8 (including all Hud$*.class) and patched jar.
- Added basegame/fulltestnonai.bat (not executed yet).
- Added tools/patch_manual.ps1 (not executed yet).
- Compiled PlayerAnimController.java, Hud.java, BuildingPlacementCursor.java with patch_manual.ps1 (2026-03-27).

Risks
- Auto-connect runs a blocking sync fetch thread; if target host is down, the connection attempt will still take up to socket timeout before returning to menu.

Risks
- Any external references/scripts may still point to old paths; update as needed.
- Multiplayer code likely has protocol and threading risks; see docs/MP_DRAFT.md.
