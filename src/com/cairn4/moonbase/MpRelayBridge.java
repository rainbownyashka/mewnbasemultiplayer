package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Minimal TCP bridge between the local game server and the public relay port.
 *
 * The bridge is hidden from players. It keeps the host's local server reachable
 * without exposing the host's home IP or any manual tunnel settings.
 */
public class MpRelayBridge {
    private final String localHost;
    private final int localPort;
    private final String relayHost;
    private final int relayPort;
    private volatile boolean running;
    private Thread worker;

    public MpRelayBridge(int localPort, String relayHost, int relayPort) {
        this("127.0.0.1", localPort, relayHost, relayPort);
    }

    public MpRelayBridge(String localHost, int localPort, String relayHost, int relayPort) {
        this.localHost = localHost != null ? localHost : "127.0.0.1";
        this.localPort = localPort;
        this.relayHost = relayHost != null ? relayHost : "";
        this.relayPort = relayPort;
    }

    public synchronized void start() {
        if (this.running) {
            return;
        }
        if (this.relayHost.trim().length() == 0 || this.relayPort <= 0) {
            Gdx.app.error("MpRelayBridge", "Relay endpoint not configured");
            return;
        }
        this.running = true;
        this.worker = new Thread(this::runLoop, "MewnBase-MP-RelayBridge");
        this.worker.setDaemon(true);
        this.worker.start();
    }

    public synchronized void stop() {
        this.running = false;
        if (this.worker != null) {
            this.worker.interrupt();
            this.worker = null;
        }
    }

    private void runLoop() {
        while (this.running) {
            Socket local = null;
            Socket relay = null;
            try {
                local = new Socket();
                local.connect(new InetSocketAddress(this.localHost, this.localPort), 5000);
                local.setTcpNoDelay(true);

                relay = new Socket();
                relay.connect(new InetSocketAddress(this.relayHost, this.relayPort), 5000);
                relay.setTcpNoDelay(true);

                Gdx.app.log("MpRelayBridge", "Bridge connected local " + this.localHost + ":" + this.localPort
                        + " <-> relay " + this.relayHost + ":" + this.relayPort);
                pumpUntilClosed(local, relay);
            } catch (Exception e) {
                if (this.running) {
                    Gdx.app.log("MpRelayBridge", "Bridge retry: " + e.getMessage());
                    sleepQuietly(2500L);
                }
            } finally {
                closeQuietly(local);
                closeQuietly(relay);
            }
        }
    }

    private void pumpUntilClosed(final Socket local, final Socket relay) throws IOException, InterruptedException {
        final Thread leftToRight = new Thread(() -> copy(local, relay), "MewnBase-MP-RelayBridge-L2R");
        final Thread rightToLeft = new Thread(() -> copy(relay, local), "MewnBase-MP-RelayBridge-R2L");
        leftToRight.setDaemon(true);
        rightToLeft.setDaemon(true);
        leftToRight.start();
        rightToLeft.start();
        while (this.running && local.isConnected() && relay.isConnected() && !local.isClosed() && !relay.isClosed()) {
            if (!leftToRight.isAlive() || !rightToLeft.isAlive()) {
                break;
            }
            Thread.sleep(100L);
        }
    }

    private void copy(Socket src, Socket dst) {
        byte[] buffer = new byte[8192];
        try {
            InputStream in = src.getInputStream();
            OutputStream out = dst.getOutputStream();
            int read;
            while (this.running && (read = in.read(buffer)) >= 0) {
                if (read > 0) {
                    out.write(buffer, 0, read);
                    out.flush();
                }
            }
        } catch (Exception ignored) {
        } finally {
            closeQuietly(src);
            closeQuietly(dst);
        }
    }

    private static void closeQuietly(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception ignored) {}
        }
    }

    private static void sleepQuietly(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
