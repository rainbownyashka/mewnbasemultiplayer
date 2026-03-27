package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;

public class NetworkHelper {
    /**
     * Send a payload either via the local client (if present) or via the active in-process server.
     * Payload should be formatted as the inner protocol (e.g., "TILE_REMOVE:..." or "ITEM_DROP:...")
     * When broadcasting from server, it will be prefixed by the server (0:...), as callers expect.
     */
    public static void sendPayload(com.cairn4.moonbase.ui.GameScreen gs, String payload) {
        try {
            if (gs != null && gs.client != null) {
                try {
                    Class<?> clientClass = gs.client.getClass();
                    java.lang.reflect.Method send = clientClass.getMethod("sendMessage", String.class);
                    send.invoke(gs.client, payload);
                    return;
                } catch (NoSuchMethodException nsme) {
                    // fallthrough to server
                }
            }
            com.cairn4.moonbase.Server s = com.cairn4.moonbase.Server.getActiveServer();
            if (s != null) {
                s.broadcastFromServer(payload);
                return;
            }
            Gdx.app.log("NetworkHelper", "No client or active server to send payload: " + payload);
        } catch (Exception e) {
            Gdx.app.error("NetworkHelper", "Failed to send payload: " + payload, e);
        }
    }
}
