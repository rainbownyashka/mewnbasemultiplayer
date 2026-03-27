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

Impact
- Original game files remain in basegame/ and should not be edited in place.
- Scripts now assume src/ for sources, out/ for compiled classes, work/ for packaging.

Verification
- Compiled MultiplayerConfigMenu.java and Server.java with javac --release 8 (classpath basegame jar, sourcepath src/com).
- Game launch not run in this environment.

Risks
- Any external references/scripts may still point to old paths; update as needed.
- Multiplayer code likely has protocol and threading risks; see docs/MP_DRAFT.md.
