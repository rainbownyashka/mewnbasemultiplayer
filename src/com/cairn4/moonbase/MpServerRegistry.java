package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight "mirror" client: hosts register their Radmin/LAN IP here;
 * players fetch the list. The game world still runs on the host PC, not on VPS.
 */
public class MpServerRegistry {
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

    private static volatile Thread heartbeatThread;

    public static String getRegistryBaseUrl() {
        String u = System.getProperty("mewnbase.mp.registry");
        if (u == null || u.trim().length() == 0) {
            return "";
        }
        u = u.trim();
        while (u.endsWith("/")) {
            u = u.substring(0, u.length() - 1);
        }
        return u;
    }

    public static String getAdvertiseHost() {
        String h = System.getProperty("mewnbase.mp.advertiseHost");
        if (h != null && h.trim().length() > 0) {
            return h.trim();
        }
        return "";
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
        request.setUrl(base + "/servers");
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
                                if (host.length() > 0) {
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

    public static void registerOnce(Server server) {
        final String base = getRegistryBaseUrl();
        final String host = getAdvertiseHost();
        if (base.length() == 0 || host.length() == 0 || server == null) {
            return;
        }
        int players = 1;
        try {
            players = 1 + server.getClientCount();
        } catch (Exception ignored) {}
        final int playersFinal = players;
        final String name = getServerDisplayName();
        final int port = server.getPort();
        Net.HttpRequest request = new Net.HttpRequest("POST");
        request.setUrl(base + "/servers");
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Accept", "application/json");
        String body = "{\"name\":\"" + jsonEscape(name) + "\",\"host\":\"" + jsonEscape(host)
                + "\",\"port\":" + port + ",\"players\":" + playersFinal + "}";
        request.setContent(body);
        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                if (httpResponse.getStatus().getStatusCode() != 200) {
                    Gdx.app.log("MpRegistry", "register HTTP " + httpResponse.getStatus().getStatusCode());
                }
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.log("MpRegistry", "register failed: " + (t != null ? t.getMessage() : "?"));
            }

            @Override
            public void cancelled() {}
        });
    }

    public static void startHeartbeatIfConfigured(final Server server) {
        final String base = getRegistryBaseUrl();
        final String host = getAdvertiseHost();
        if (base.length() == 0 || host.length() == 0) {
            return;
        }
        synchronized (MpServerRegistry.class) {
            if (heartbeatThread != null && heartbeatThread.isAlive()) {
                return;
            }
            heartbeatThread = new Thread(() -> {
                while (server != null && server.isRunning()) {
                    try {
                        registerOnce(server);
                    } catch (Exception ignored) {}
                    try {
                        Thread.sleep(45000L);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }, "MewnBase-Registry-Heartbeat");
            heartbeatThread.setDaemon(true);
            heartbeatThread.start();
        }
        registerOnce(server);
    }

    public static void stopHeartbeat() {
        synchronized (MpServerRegistry.class) {
            if (heartbeatThread != null) {
                heartbeatThread.interrupt();
                heartbeatThread = null;
            }
        }
    }

    private static String jsonEscape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }
}
