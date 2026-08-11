#!/usr/bin/env python3
import argparse
import json
import queue
import signal
import socket
import socketserver
import threading
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import urlparse


class Session:
    def __init__(self, registry, port, name, players):
        self.registry = registry
        self.port = port
        self.name = name or "MewnBase Server"
        self.players = int(players or 0)
        self.created_at = time.time()
        self.updated_at = self.created_at
        self.running = True
        self.listener = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.listener.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.listener.bind((registry.bind_host, port))
        self.listener.listen(8)
        self.listener.settimeout(1.0)
        self.waiting = None
        self.lock = threading.Lock()
        self.thread = threading.Thread(target=self._accept_loop, name=f"relay-session-{port}", daemon=True)
        self.thread.start()

    def summary(self):
        return {
            "name": self.name,
            "host": self.registry.public_host,
            "port": self.port,
            "players": self.players,
        }

    def update(self, payload):
        if "name" in payload and payload["name"]:
            self.name = str(payload["name"])
        if "players" in payload:
            try:
                self.players = int(payload["players"])
            except Exception:
                pass
        self.updated_at = time.time()

    def close(self):
        self.running = False
        try:
            self.listener.close()
        except Exception:
            pass
        with self.lock:
            if self.waiting is not None:
                try:
                    self.waiting.close()
                except Exception:
                    pass
                self.waiting = None

    def _accept_loop(self):
        while self.running:
            try:
                conn, _addr = self.listener.accept()
            except socket.timeout:
                continue
            except Exception:
                break
            conn.setsockopt(socket.IPPROTO_TCP, socket.TCP_NODELAY, 1)
            conn.settimeout(None)
            peer = None
            with self.lock:
                if self.waiting is None:
                    self.waiting = conn
                else:
                    peer = self.waiting
                    self.waiting = None
            if peer is not None:
                self._start_pair(peer, conn)

    def _start_pair(self, a, b):
        def pump(src, dst):
            try:
                while self.running:
                    data = src.recv(8192)
                    if not data:
                        break
                    dst.sendall(data)
            except Exception:
                pass
            finally:
                for s in (src, dst):
                    try:
                        s.close()
                    except Exception:
                        pass

        threading.Thread(target=pump, args=(a, b), daemon=True).start()
        threading.Thread(target=pump, args=(b, a), daemon=True).start()


class Registry:
    def __init__(self, bind_host, public_host, min_port, max_port, ttl_sec):
        self.bind_host = bind_host
        self.public_host = public_host
        self.min_port = min_port
        self.max_port = max_port
        self.ttl_sec = max(10, int(ttl_sec or 0))
        self.sessions = {}
        self.lock = threading.Lock()

    def allocate_port(self):
        with self.lock:
            used = set(self.sessions.keys())
            for port in range(self.min_port, self.max_port + 1):
                if port not in used:
                    return port
        raise RuntimeError("No free relay ports left")

    def create_session(self, payload):
        port = self.allocate_port()
        session = Session(self, port, payload.get("name"), payload.get("players", 0))
        session.update(payload)
        with self.lock:
            self.sessions[port] = session
        return session

    def purge_expired(self):
        now = time.time()
        expired = []
        with self.lock:
            for port, session in list(self.sessions.items()):
                if now - session.updated_at > self.ttl_sec:
                    expired.append(port)
            for port in expired:
                session = self.sessions.pop(port, None)
                if session is not None:
                    session.close()
        return expired

    def list_sessions(self):
        self.purge_expired()
        with self.lock:
            return [s.summary() for s in self.sessions.values()]

    def list_legacy_servers(self):
        self.purge_expired()
        with self.lock:
            return [
                {
                    "name": s.name,
                    "host": s.registry.public_host,
                    "port": s.port,
                    "players": s.players,
                    "updated": s.updated_at,
                }
                for s in self.sessions.values()
            ]

    def update_session(self, port, payload):
        with self.lock:
            session = self.sessions.get(port)
        if session is None:
            return None
        session.update(payload)
        return session

    def delete_session(self, port):
        with self.lock:
            session = self.sessions.pop(port, None)
        if session is not None:
            session.close()
        return session is not None


class Handler(BaseHTTPRequestHandler):
    registry = None

    def _send_json(self, code, payload):
        body = json.dumps(payload).encode("utf-8")
        self.send_response(code)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def _read_json(self):
        length = int(self.headers.get("Content-Length", "0") or "0")
        if length <= 0:
            return {}
        raw = self.rfile.read(length)
        if not raw:
            return {}
        return json.loads(raw.decode("utf-8"))

    def log_message(self, fmt, *args):
        return

    def do_GET(self):
        path = urlparse(self.path).path
        if path in ("/health", "/health/"):
            self._send_json(200, {"ok": True})
            return
        if path in ("/sessions", "/sessions/"):
            self._send_json(200, self.registry.list_sessions())
            return
        if path in ("/servers", "/servers/"):
            self._send_json(200, self.registry.list_legacy_servers())
            return
        self._send_json(404, {"error": "not found"})

    def do_POST(self):
        path = urlparse(self.path).path
        if path == "/sessions":
            try:
                payload = self._read_json()
                session = self.registry.create_session(payload)
                self._send_json(200, session.summary())
            except Exception as e:
                self._send_json(500, {"error": str(e)})
            return
        self._send_json(404, {"error": "not found"})

    def do_PUT(self):
        path = urlparse(self.path).path
        if path.startswith("/sessions/"):
            try:
                port = int(path.rsplit("/", 1)[-1])
                payload = self._read_json()
                session = self.registry.update_session(port, payload)
                if session is None:
                    self._send_json(404, {"error": "session not found"})
                else:
                    self._send_json(200, session.summary())
            except Exception as e:
                self._send_json(500, {"error": str(e)})
            return
        self._send_json(404, {"error": "not found"})

    def do_DELETE(self):
        path = urlparse(self.path).path
        if path.startswith("/sessions/"):
            try:
                port = int(path.rsplit("/", 1)[-1])
                deleted = self.registry.delete_session(port)
                if deleted:
                    self._send_json(200, {"ok": True})
                else:
                    self._send_json(404, {"error": "session not found"})
            except Exception as e:
                self._send_json(500, {"error": str(e)})
            return
        self._send_json(404, {"error": "not found"})


def main():
    parser = argparse.ArgumentParser(description="MewnBase relay/listing server")
    parser.add_argument("--http-host", "--host", default="0.0.0.0")
    parser.add_argument("--http-port", "--port", type=int, default=8080)
    parser.add_argument("--bind-host", default="0.0.0.0", help="host to bind per-session TCP relay ports")
    parser.add_argument("--public-host", default="mewnbase.clovergame.org", help="host returned to players in the session list")
    parser.add_argument("--min-port", type=int, default=12000)
    parser.add_argument("--max-port", type=int, default=12999)
    parser.add_argument("--ttl", type=int, default=45, help="drop sessions after N seconds without heartbeat")
    args = parser.parse_args()

    registry = Registry(args.bind_host, args.public_host, args.min_port, args.max_port, args.ttl)
    Handler.registry = registry
    server = ThreadingHTTPServer((args.http_host, args.http_port), Handler)

    stop_event = threading.Event()

    def reap_loop():
        while not stop_event.wait(5.0):
            try:
                registry.purge_expired()
            except Exception:
                pass

    reaper = threading.Thread(target=reap_loop, name="relay-reaper", daemon=True)
    reaper.start()

    def shutdown(*_args):
        stop_event.set()
        server.shutdown()
        with registry.lock:
            sessions = list(registry.sessions.values())
        for session in sessions:
            session.close()

    signal.signal(signal.SIGINT, shutdown)
    signal.signal(signal.SIGTERM, shutdown)

    print(f"HTTP listening on {args.http_host}:{args.http_port}")
    print(f"Relay ports on {args.bind_host}:{args.min_port}-{args.max_port}, public host {args.public_host}")
    try:
        server.serve_forever(poll_interval=0.5)
    finally:
        shutdown()
        server.server_close()


if __name__ == "__main__":
    main()
