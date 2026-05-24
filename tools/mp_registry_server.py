#!/usr/bin/env python3
"""
Lightweight MP server-list mirror (~20-50 MB RAM).
Does NOT run the game — only stores host IP:port for easy connect.

  python tools/mp_registry_server.py
  python tools/mp_registry_server.py --port 8080 --ttl 90

API:
  GET  /servers          -> JSON list of live servers
  POST /servers          -> register {"name","host","port","players"}
  GET  /health           -> {"ok":true}
"""

from __future__ import annotations

import argparse
import json
import threading
import time
from http.server import BaseHTTPRequestHandler, HTTPServer
from typing import Any, Dict

SERVERS: Dict[str, Dict[str, Any]] = {}
LOCK = threading.Lock()
TTL_SEC = 90


def prune() -> None:
    now = time.time()
    dead = [k for k, v in SERVERS.items() if now - float(v.get("updated", 0)) > TTL_SEC]
    for k in dead:
        del SERVERS[k]


class Handler(BaseHTTPRequestHandler):
    def log_message(self, format: str, *args: Any) -> None:
        pass

    def _cors(self) -> None:
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type")

    def _json(self, code: int, obj: Any) -> None:
        body = json.dumps(obj).encode("utf-8")
        self.send_response(code)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self._cors()
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_OPTIONS(self) -> None:
        self.send_response(204)
        self._cors()
        self.end_headers()

    def do_GET(self) -> None:
        if self.path in ("/health", "/health/"):
            self._json(200, {"ok": True})
            return
        if self.path in ("/servers", "/servers/"):
            with LOCK:
                prune()
                rows = sorted(SERVERS.values(), key=lambda r: r.get("name", ""))
            self._json(200, rows)
            return
        self._json(404, {"error": "not found"})

    def do_POST(self) -> None:
        if self.path not in ("/servers", "/servers/"):
            self._json(404, {"error": "not found"})
            return
        try:
            n = int(self.headers.get("Content-Length", "0"))
            raw = self.rfile.read(n) if n > 0 else b"{}"
            body = json.loads(raw.decode("utf-8"))
        except Exception as e:
            self._json(400, {"error": str(e)})
            return
        host = str(body.get("host", "")).strip()
        port = int(body.get("port", 7777))
        if not host or port < 1 or port > 65535:
            self._json(400, {"error": "host and port required"})
            return
        row = {
            "name": str(body.get("name", "Server"))[:64],
            "host": host,
            "port": port,
            "players": max(0, int(body.get("players", 1))),
            "updated": time.time(),
        }
        key = f"{host}:{port}"
        with LOCK:
            SERVERS[key] = row
        self._json(200, {"ok": True, "key": key})


def main() -> None:
    global TTL_SEC
    p = argparse.ArgumentParser(description="MewnBase MP registry mirror")
    p.add_argument("--host", default="0.0.0.0")
    p.add_argument("--port", type=int, default=8080)
    p.add_argument("--ttl", type=int, default=90, help="drop entries after N seconds without heartbeat")
    args = p.parse_args()
    TTL_SEC = max(30, args.ttl)
    httpd = HTTPServer((args.host, args.port), Handler)
    print(f"MP registry mirror on http://{args.host}:{args.port}  (TTL {TTL_SEC}s, RAM ~few MB)")
    print("GET /servers   POST /servers   GET /health")
    httpd.serve_forever()


if __name__ == "__main__":
    main()
