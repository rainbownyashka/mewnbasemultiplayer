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

Impact
- Original game files remain in basegame/ and should not be edited in place.
- Scripts now assume src/ for sources, out/ for compiled classes, work/ for packaging.

Verification
- Compiled MultiplayerConfigMenu.java and Server.java with javac --release 8 (classpath basegame jar, sourcepath src/com).
- Compiled MoonBase.java, debug/RuntimeEval.java, and debug/UiTestServer.java with javac --release 8.
- Compiled updated UiTestServer.java with javac --release 8 and patched jar.
- Compiled MainMenu.java and MultiplayerConfigMenu.java with javac --release 8 and patched jar.
- Compiled Hud.java with javac --release 8 and patched jar.
- Compiled MoonBase.java and MainMenu.java with javac --release 8 and patched jar.
- Compiled MultiplayerConfigMenu.java with javac --release 8 and patched jar (including inner classes).
- Compiled DesktopLauncher.java with javac --release 8 and patched jar.
- Updated tools/mewnbase_autoupdate_ai.py (not executed yet).
- Updated basegame/run_client.bat (not executed yet).
- Compiled MultiplayerConfigMenu.java with javac --release 8 and patched jar (including inner classes).
- UI test via UiTestServer: MainMenu -> Multiplayer -> Create Server (7777) -> GameScreen.

Risks
- Auto-connect runs a blocking sync fetch thread; if target host is down, the connection attempt will still take up to socket timeout before returning to menu.

Risks
- Any external references/scripts may still point to old paths; update as needed.
- Multiplayer code likely has protocol and threading risks; see docs/MP_DRAFT.md.
