package com.cairn4.moonbase.net;

import java.net.URLDecoder;
import java.net.URLEncoder;

public class ProtocolV2 {
    public static final String PREFIX = "MB2";
    public static final String VERSION = "2";

    public static String encode(int fromId, String type, String payload) {
        try {
            if (type == null) type = "";
            if (payload == null) payload = "";
            String encPayload = URLEncoder.encode(payload, "UTF-8");
            return PREFIX + "|" + fromId + "|" + type + "|" + encPayload;
        } catch (Exception e) {
            return null;
        }
    }

    public static Decoded decode(String frame) {
        try {
            if (frame == null) return null;
            if (!frame.startsWith(PREFIX + "|")) return null;
            String[] parts = frame.split("\\|", 4);
            if (parts.length < 4) return null;
            int fromId;
            try { fromId = Integer.parseInt(parts[1]); } catch (Exception e) { return null; }
            String type = parts[2];
            String payload = "";
            try { payload = URLDecoder.decode(parts[3], "UTF-8"); } catch (Exception ignored) {}
            return new Decoded(fromId, type, payload);
        } catch (Exception e) {
            return null;
        }
    }

    public static class Decoded {
        public final int fromId;
        public final String type;
        public final String payload;

        public Decoded(int fromId, String type, String payload) {
            this.fromId = fromId;
            this.type = type;
            this.payload = payload;
        }
    }
}
