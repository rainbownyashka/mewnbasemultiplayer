package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.util.ArrayList;
import java.util.List;

/**
 * VPS-backed multiplayer session registry.
 *
 * Hosts create a session here, which yields a public relay endpoint.
 * Guests fetch the session list from the same service and connect to the
 * public endpoint rather than the host's home IP.
 */
public class MpServerRegistry {
    private static final String DEFAULT_REGISTRY_BASE_URL = "http://mewnbase.clovergame.org:8080";
    private static final long HEARTBEAT_INTERVAL_MS = 15000L;
    private static volatile Thread heartbeatThread;
    private static volatile Entry activeSession;
    private static volatile MpRelayBridge activeBridge;

    public static final class Entry {
        public final String name;
        public final String host;
        public final int port;
        public final int players;

        public Entry(String name, String host, int port, int players) {
            this.name = name != null ? name : "";
            this.host = host != null ? host : "";
            this.port = port;
            this.players = players;
        }
    }

    public interface ListCallback {
        void onResult(List<Entry> entries, String error);
    }

    public static String getRegistryBaseUrl() {
        String u = System.getProperty("mewnbase.mp.registry");
        if (u == null || u.trim().length() == 0) {
            u = DEFAULT_REGISTRY_BASE_URL;
        }
        u = u.trim();
        while (u.endsWith("/")) {
            u = u.substring(0, u.length() - 1);
        }
        return u;
    }

    public static String getServerDisplayName() {
        String n = System.getProperty("mewnbase.mp.serverName");
        if (n != null && n.trim().length() > 0) {
            return n.trim();
        }
        try {
            String nick = MoonBase.multiplayerNick;
            if (nick != null && nick.trim().length() > 0) {
                return nick.trim() + "'s world";
            }
        } catch (Exception ignored) {}
        return "MewnBase Server";
    }

    public static void fetchServerList(final ListCallback callback) {
        final String base = getRegistryBaseUrl();
        if (base.length() == 0) {
            if (callback != null) {
                callback.onResult(new ArrayList<Entry>(), "Registry URL not set (-Dmewnbase.mp.registry=...)");
            }
            return;
        }

        Net.HttpRequest request = new Net.HttpRequest("GET");
        request.setUrl(base + "/sessions");
        request.setHeader("Accept", "application/json");
        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                List<Entry> out = new ArrayList<Entry>();
                String err = null;
                try {
                    if (httpResponse.getStatus().getStatusCode() != 200) {
                        err = "HTTP " + httpResponse.getStatus().getStatusCode();
                    } else {
                        JsonValue root = new JsonReader().parse(httpResponse.getResultAsString());
                        if (root != null && root.isArray()) {
                            for (JsonValue row : root) {
                                String name = row.getString("name", "");
                                String host = row.getString("host", "");
                                int port = row.getInt("port", 7777);
                                int players = row.getInt("players", 0);
                                if (host.length() > 0 && port > 0) {
                                    out.add(new Entry(name, host, port, players));
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    err = e.getMessage();
                }
                final List<Entry> result = out;
                final String error = err;
                Gdx.app.postRunnable(() -> {
                    if (callback != null) {
                        callback.onResult(result, error);
                    }
                });
            }

            @Override
            public void failed(Throwable t) {
                final String msg = t != null ? t.getMessage() : "request failed";
                Gdx.app.postRunnable(() -> {
                    if (callback != null) {
                        callback.onResult(new ArrayList<Entry>(), msg);
                    }
                });
            }

            @Override
            public void cancelled() {
                failed(new Exception("cancelled"));
            }
        });
    }

    private static Entry parseSessionResponse(String body) {
        JsonValue root = new JsonReader().parse(body);
        if (root == null) {
            return null;
        }
        String name = root.getString("name", "");
        String host = root.getString("host", "");
        int port = root.getInt("port", 7777);
        int players = root.getInt("players", 0);
        if (host.length() == 0 || port <= 0) {
            return null;
        }
        return new Entry(name, host, port, players);
    }

    public static Entry createSession(Server server) {
        final String base = getRegistryBaseUrl();
        if (base.length() == 0 || server == null) {
            return null;
        }

        int players = 1;
        try {
            players = 1 + server.getClientCount();
        } catch (Exception ignored) {}
        final String name = getServerDisplayName();
        final int localPort = server.getPort();
        String saveFolder = null;
        try {
            saveFolder = MoonBase.currentSaveFolder;
        } catch (Exception ignored) {}

        Net.HttpRequest request = new Net.HttpRequest("POST");
        request.setUrl(base + "/sessions");
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Accept", "application/json");
        StringBuilder body = new StringBuilder();
        body.append("{");
        body.append("\"name\":\"").append(jsonEscape(name)).append("\",");
        body.append("\"players\":").append(players).append(",");
        body.append("\"localPort\":").append(localPort);
        if (saveFolder != null && saveFolder.trim().length() > 0) {
            body.append(",\"saveFolder\":\"").append(jsonEscape(saveFolder.trim())).append("\"");
        }
        body.append("}");
        request.setContent(body.toString());

        final String[] responseBody = new String[]{null};
        final int[] responseCode = new int[]{-1};
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                try {
                    responseCode[0] = httpResponse.getStatus().getStatusCode();
                    responseBody[0] = httpResponse.getResultAsString();
                } finally {
                    latch.countDown();
                }
            }

            @Override
            public void failed(Throwable t) {
                responseBody[0] = null;
                latch.countDown();
            }

            @Override
            public void cancelled() {
                responseBody[0] = null;
                latch.countDown();
            }
        });

        try {
            if (!latch.await(5000L, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                Gdx.app.error("MpRegistry", "Session creation timed out");
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }

        if (responseCode[0] != 200 || responseBody[0] == null || responseBody[0].trim().length() == 0) {
            Gdx.app.error("MpRegistry", "Session creation failed, HTTP " + responseCode[0]);
            return null;
        }

        try {
            Entry entry = parseSessionResponse(responseBody[0]);
            if (entry != null) {
                Gdx.app.log("MpRegistry", "Created session " + entry.host + ":" + entry.port);
            }
            return entry;
        } catch (Exception e) {
            Gdx.app.error("MpRegistry", "Invalid session response", e);
            return null;
        }
    }

    public static void startHeartbeatIfConfigured(final Server server) {
        final String base = getRegistryBaseUrl();
        if (base.length() == 0 || server == null) {
            return;
        }
        if (!isOnlineHostMode()) {
            return;
        }

        synchronized (MpServerRegistry.class) {
            if (activeSession == null) {
                activeSession = createSession(server);
            }
            if (activeSession == null) {
                Gdx.app.error("MpRegistry", "Unable to create online relay session");
                return;
            }
            if (activeBridge == null) {
                activeBridge = new MpRelayBridge(server.getPort(), activeSession.host, activeSession.port);
                activeBridge.start();
            }
            if (heartbeatThread != null && heartbeatThread.isAlive()) {
                return;
            }
            heartbeatThread = new Thread(() -> {
                while (server != null && server.isRunning()) {
                    try {
                        updateSession(activeSession, server);
                    } catch (Exception ignored) {}
                    try {
                        Thread.sleep(HEARTBEAT_INTERVAL_MS);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }, "MewnBase-Relay-Heartbeat");
            heartbeatThread.setDaemon(true);
            heartbeatThread.start();
        }
        updateSession(activeSession, server);
    }

    public static void stopHeartbeat() {
        synchronized (MpServerRegistry.class) {
            if (heartbeatThread != null) {
                heartbeatThread.interrupt();
                heartbeatThread = null;
            }
            if (activeBridge != null) {
                activeBridge.stop();
                activeBridge = null;
            }
            if (activeSession != null) {
                deleteSession(activeSession);
                activeSession = null;
            }
        }
    }

    private static boolean isOnlineHostMode() {
        try {
            MoonBase.PendingHostServerOptions opt = MoonBase.pendingHostServerOptions;
            return opt != null && opt.networkMode == MoonBase.PendingHostServerOptions.NetworkMode.ONLINE;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static void updateSession(Entry session, Server server) {
        if (session == null || server == null) {
            return;
        }
        final String base = getRegistryBaseUrl();
        if (base.length() == 0) {
            return;
        }
        int players = 1;
        try {
            players = 1 + server.getClientCount();
        } catch (Exception ignored) {}
        Net.HttpRequest request = new Net.HttpRequest("PUT");
        request.setUrl(base + "/sessions/" + session.port);
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Accept", "application/json");
        request.setContent("{\"players\":" + players + "}");
        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                if (httpResponse.getStatus().getStatusCode() != 200) {
                    Gdx.app.log("MpRegistry", "heartbeat HTTP " + httpResponse.getStatus().getStatusCode());
                }
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.log("MpRegistry", "heartbeat failed: " + (t != null ? t.getMessage() : "?"));
            }

            @Override
            public void cancelled() {}
        });
    }

    private static void deleteSession(Entry session) {
        if (session == null) {
            return;
        }
        final String base = getRegistryBaseUrl();
        if (base.length() == 0) {
            return;
        }
        Net.HttpRequest request = new Net.HttpRequest("DELETE");
        request.setUrl(base + "/sessions/" + session.port);
        request.setHeader("Accept", "application/json");
        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {}

            @Override
            public void failed(Throwable t) {}

            @Override
            public void cancelled() {}
        });
    }

    private static String jsonEscape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }
}
