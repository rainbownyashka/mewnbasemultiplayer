# Relay/listing service on VPS

## Idea

The VPS does **not** run the game world. It only runs a tiny HTTP + TCP relay service:

- HTTP API returns the list of active online sessions.
- Each session gets its own public relay port.
- The host's game starts a hidden bridge that connects the local `127.0.0.1:7777` server to that public port.
- Guests join through **Browse online list** and connect to the relay endpoint, not to the host's home IP.

| Where | What it does | RAM |
|-----|------------|-----|
| **VPS** | `tools/mp_relay_server.py` — session list + per-session TCP relay | ~20-50 MB |
| **Host PC** | MewnBase + hidden relay bridge + local server on `7777` | as usual |
| **Guest PC** | **Browse online list** -> select session -> Connect | as usual |

## Running the service

```bash
python3 tools/mp_relay_server.py --http-host 0.0.0.0 --http-port 8080 --public-host mewnbase.clovergame.org
```

Defaults:

- HTTP/listing: `8080/tcp`
- relay ports: `12000-12999`
- public host returned to players: `mewnbase.clovergame.org`

If you need different ports, pass `--min-port` / `--max-port`.

## systemd example

```ini
[Unit]
Description=MewnBase relay/listing service
After=network.target

[Service]
Type=simple
WorkingDirectory=/opt/mewnbasemultiplayer
ExecStart=/usr/bin/python3 tools/mp_relay_server.py --http-port 8080 --public-host mewnbase.clovergame.org
Restart=always

[Install]
WantedBy=multi-user.target
```

## Host flow

1. Open **MULTIPLAYER** in the game.
2. Choose **Create Server (7777)**.
3. Pick **Online** in host setup and select a save.
4. The game creates a relay session automatically and starts the hidden bridge.

No manual tunnel command, SSH setup, or host IP sharing is required.

## Guest flow

1. Open **MULTIPLAYER** in the game.
2. Choose **Browse online list**.
3. Pick a session and press **Connect**.

The guest connects to the public relay port returned by the service.

## API shape

- `GET /sessions` returns JSON list of active sessions.
- `POST /sessions` creates a new session and returns its public endpoint.
- `PUT /sessions/<port>` updates the visible player count/name.
- `DELETE /sessions/<port>` removes the session.

The game currently uses the default registry URL:

```text
http://mewnbase.clovergame.org:8080
```

You can override it with `-Dmewnbase.mp.registry=<url>`.

## Troubleshooting

If the session does not appear:

1. Verify the host fully loaded the save before opening the online list.
2. Check the host log for relay session creation errors.
3. Check the VPS service logs for HTTP 5xx or relay port allocation failures.
4. Make sure the public relay port range is open on the VPS firewall.

If connection hangs:

1. Confirm the session exists in `GET /sessions`.
2. Confirm the host bridge is still running.
3. Restart the session by closing and reopening the host server.
