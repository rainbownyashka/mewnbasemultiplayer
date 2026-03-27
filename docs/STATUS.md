# Status

Last update: 2026-03-27

Changes
- Restructured workspace to a simpler layout (basegame/, src/, work/, out/, tools/, backups/, old/).
- Updated autocompile script paths to new layout and moved it under tools/.
- Root run script now calls tools/mewnbase_autoupdate.py.
- Generated a diff list of multiplayer-related changes between old decompiled sources and current src (12 files).
- Audited all old decompiled variants vs current src and captured counts in old/_diff_summary.txt.
- Added multiplayer review draft in docs/MP_DRAFT.md.

Impact
- Original game files remain in basegame/ and should not be edited in place.
- Scripts now assume src/ for sources, out/ for compiled classes, work/ for packaging.

Verification
- Not run (no compilation/test executed yet).

Risks
- Any external references/scripts may still point to old paths; update as needed.
- Multiplayer code likely has protocol and threading risks; see docs/MP_DRAFT.md.
