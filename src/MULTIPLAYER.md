# MewnBase Multiplayer — Overview

This document summarizes the decompiled multiplayer code found in this project and gives quick instructions for running and improving the build/run process.

## Key classes
- `com.cairn4.moonbase.Client` — TCP client used by game instances to connect to a server. Uses `Socket`, `DataInputStream`/`DataOutputStream` and sends/receives UTF-framed messages. Has a reader thread and a sender thread (periodic POS updates).
- `com.cairn4.moonbase.Server` — Simple TCP server listening for client connections. Accepts connections, assigns incremental client IDs, and spawns `ClientHandler` instances to manage each client.
- `com.cairn4.moonbase.Server.ClientHandler` — Handles per-client I/O, initial handshake (sends clientId and optional save/world bytes), relays messages to other clients and can apply certain messages to an attached `GameScreen` when running in-process.
- `com.cairn4.moonbase.MoonBase` — Global configuration holder. Contains `isMultiplayer`, `multiplayerHost`, `multiplayerPort`, `multiplayerNick`, and other globals.
- `com.cairn4.moonbase.ui.GameScreen` — (Not duplicated here) interacts with `Client` when `MoonBase.isMultiplayer` is set; creates client via reflection and receives calls like `addPlayer`, `updatePlayerPosition`, `updatePlayerAppearance`, `removePlayer`.

## Message protocol (text-based over writeUTF/readUTF)
Client and server exchange simple UTF messages framed by Java's `Data{In,Out}putStream.writeUTF/readUTF`.
Common payloads (examples):
- `SPAWNREMOTE` — client announces presence; server rebroadcasts as `<clientId>:SPAWNREMOTE`.
- `CONNECTED:<id>` / `DISCONNECTED:<id>` — server notifies about clients joining/leaving.
- `APPEARANCE:<face>|<color>|<nick>` — appearance payload. Server may request with `REQUEST_APPEARANCE`.
- `POS:<x>:<y>` — periodic player position updates broadcasted to other clients.
- `TILE_SET:...`, `TILE_BUILD_START:...`, `TILE_REMOVE:...` — world update commands that the server relays and can apply to host `GameScreen` when server runs in-process.
- `PING:...` — debug/latency messages.

Additionally, the server may send an optional binary handshake at connection start (before UTF stream):
- `writeInt(clientId)`
- `writeInt(gameSaveBytes.length); if >0 write(gameSaveBytes)`
- `writeInt(worldDataBytes.length); if >0 write(worldDataBytes)`

Client handles this by probing with a short `soTimeout` and reading ints and optional blocks.

## Behavior notes
- `Server` assigns incremental client IDs starting at 1; host is ID 0 for server-origin messages.
- `GameScreen` uses reflection to create `Client` at runtime to avoid compile-time dependency; it will attempt to call `connect()` on the client instance.
- When server runs in-process (started by game), it can call `setGameScreen(GameScreen)` to attach host rendering and to apply world updates locally.
- Message duplication suppression exists in `Client` to avoid processing identical messages within 250ms.
- `Client` implements a diagnostic mode to raw-read bytes when UTF timeouts occur — helpful for reverse-engineering or debugging malformed framing.

## Files to inspect next (important locations)
- `com/cairn4/moonbase/Client.java`
- `com/cairn4/moonbase/Server.java`
- `com/cairn4/moonbase/MoonBase.java`
- `com/cairn4/moonbase/ui/GameScreen.java` (interactions with Client)
- `com/cairn4/moonbase/ui/MultiplayerConfigMenu.java` (menu sets `MoonBase.multiplayerHost/Port` and `isMultiplayer`)

## How to run the server and client
Quick manual runs (from compiled JAR or IDE):
- Start server: create `new Server(PORT); server.start();` — or run the game's menu option that starts an in-process server.
- Start client: create `new Client(host, port, gameScreen); client.connect();`.

Because the project is decompiled, the current manual compile command you mentioned may be fragile. I recommend creating a small `build.bat` that compiles all `.java` files with a consistent classpath rather than individually compiling files.

Example `build.bat` (Windows PowerShell-friendly):

```powershell
# from project root (where 'com' folder resides)
$src = "src" # adapt if source is in 'decompiled' folder
$dest = "bin"
mkdir $dest -Force
$cp = "lib/*" # put all jars (gdx, lwjgl, etc.) in lib folder
javac -d $dest -cp $cp -sourcepath $src (Get-ChildItem -Recurse -Filter *.java -Path $src | ForEach-Object { $_.FullName })
```

(Adjust classpath and source folder — I can generate a working `build.bat` tailored to your environment if you want.)

## Recommended improvements
1. Add a Gradle build (preferred) or Maven to manage dependencies (libGDX, LWJGL) and provide stable compile/run tasks. I can scaffold a `build.gradle` that compiles and produces a runnable jar for dev/testing.
2. Add startup scripts: `run-server.bat` to start the server headlessly and `run-client.bat` to launch game with multiplayer flags.
3. Add tests: a minimal integration test that starts a `Server` on localhost and spins up a `Client` to validate handshake and SPAWNREMOTE flow.
4. Add logging toggles and a `--multiplayer` CLI flag to avoid relying on UI menus for enabling.

## Next steps I can do for you
- Create `decompiled/MULTIPLAYER.md` (done).
- Generate a simple `build.bat` or a `build.gradle` that compiles sources — include classpath entries for required jars. (I can inspect your `mewnbase_dc` for native libs and suggest which jars to reference.)
- Produce small integration test harness to smoke-test server+client locally.

If you'd like, tell me which next step to perform (create `build.bat`, scaffold Gradle, or add tests).

## Compilation check command (your script)
You asked to remember this command; it checks whether the last modified/compiled file compiled successfully:

```
python "C:\\Users\\ASUS\\Documents\\mewnbase\\check_status.py"
```

Run this after compiling a file to have the project check its status.