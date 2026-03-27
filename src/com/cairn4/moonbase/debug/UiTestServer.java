package com.cairn4.moonbase.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ui.BaseScreen;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Local-only UI test server that can list and click buttons.
 * Enabled only with -Dmewnbase.uitest=1
 */
public final class UiTestServer {
    private static final AtomicBoolean started = new AtomicBoolean(false);

    public static void startIfEnabled() {
        String enabled = System.getProperty("mewnbase.uitest");
        if (!"1".equals(enabled)) return;
        if (!started.compareAndSet(false, true)) return;

        String portProp = System.getProperty("mewnbase.uitest.port", "8792");
        int portVal = 8792;
        try { portVal = Integer.parseInt(portProp); } catch (Exception ignored) {}
        final int port = portVal;
        final String token = System.getProperty("mewnbase.uitest.token", "");

        try {
            Thread t = new Thread(() -> runServer(port, token), "MewnBase-UiTestServer");
            t.setDaemon(true);
            t.start();
            try { Gdx.app.log("UiTest", "Enabled on localhost:" + port + " (token required=" + (token.length() > 0) + ")"); } catch (Exception ignored) {}
        } catch (Exception e) {
            try { Gdx.app.error("UiTest", "Failed to start UI test server", e); } catch (Exception ignored) {}
        }
    }

    private static void runServer(int port, String token) {
        try (ServerSocket server = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"))) {
            while (true) {
                try (Socket s = server.accept();
                     BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
                     PrintWriter pw = new PrintWriter(s.getOutputStream(), true)) {

                    String first = br.readLine();
                    if (first == null) continue;
                    String line;
                    if (token.length() > 0) {
                        if (!token.equals(first)) {
                            pw.println("ERR: bad token");
                            continue;
                        }
                        line = br.readLine();
                        if (line == null) { pw.println("ERR: empty"); continue; }
                    } else {
                        line = first;
                    }

                    String response = handleCommand(line);
                    pw.println(response);
                } catch (Exception e) {
                    try { Gdx.app.error("UiTest", "Session error", e); } catch (Exception ignored) {}
                }
            }
        } catch (Exception e) {
            try { Gdx.app.error("UiTest", "Server failed", e); } catch (Exception ignored) {}
        }
    }

    private static String handleCommand(String line) {
        if (line == null) return "ERR: empty";
        String cmd = line.trim();
        if (cmd.length() == 0) return "ERR: empty";
        if ("PING".equalsIgnoreCase(cmd)) return "OK";
        if ("SCREEN".equalsIgnoreCase(cmd)) return getScreenName();
        if ("LIST".equalsIgnoreCase(cmd)) return listButtons();
        if ("COUNT".equalsIgnoreCase(cmd)) return String.valueOf(getButtons().size());
        if (cmd.startsWith("CLICK_INDEX ")) {
            String idxStr = cmd.substring("CLICK_INDEX ".length()).trim();
            try {
                int idx = Integer.parseInt(idxStr);
                return clickIndex(idx);
            } catch (Exception e) {
                return "ERR: bad index";
            }
        }
        if (cmd.startsWith("CLICK ")) {
            String text = cmd.substring("CLICK ".length()).trim();
            return clickText(text);
        }
        return "ERR: unknown";
    }

    private static String getScreenName() {
        try {
            MoonBase mb = MoonBase.instance;
            if (mb == null || mb.getScreen() == null) return "NONE";
            return mb.getScreen().getClass().getName();
        } catch (Exception e) {
            return "ERR";
        }
    }

    private static String listButtons() {
        List<TextButton> buttons = getButtons();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < buttons.size(); i++) {
            TextButton b = buttons.get(i);
            sb.append(i).append(":").append(b.getText()).append("\n");
        }
        return sb.toString().trim();
    }

    private static List<TextButton> getButtons() {
        List<TextButton> out = new ArrayList<TextButton>();
        try {
            MoonBase mb = MoonBase.instance;
            if (mb == null || mb.getScreen() == null) return out;
            if (!(mb.getScreen() instanceof BaseScreen)) return out;
            BaseScreen bs = (BaseScreen) mb.getScreen();
            Stage stage = bs.stage;
            if (stage == null) return out;
            for (Actor a : stage.getActors()) {
                collectButtons(a, out);
            }
        } catch (Exception ignored) {}
        return out;
    }

    private static void collectButtons(Actor a, List<TextButton> out) {
        if (a instanceof TextButton) {
            out.add((TextButton)a);
        }
        if (a instanceof Group) {
            Group g = (Group)a;
            for (Actor child : g.getChildren()) {
                collectButtons(child, out);
            }
        }
    }

    private static String clickIndex(int idx) {
        List<TextButton> buttons = getButtons();
        if (idx < 0 || idx >= buttons.size()) return "ERR: out_of_range";
        TextButton b = buttons.get(idx);
        clickButton(b);
        return "OK";
    }

    private static String clickText(String text) {
        if (text == null) return "ERR: empty";
        List<TextButton> buttons = getButtons();
        for (TextButton b : buttons) {
            if (text.equalsIgnoreCase(String.valueOf(b.getText()))) {
                clickButton(b);
                return "OK";
            }
        }
        return "ERR: not_found";
    }

    private static void clickButton(final TextButton b) {
        if (b == null) return;
        try {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    try {
                        InputEvent down = new InputEvent();
                        down.setType(InputEvent.Type.touchDown);
                        b.fire(down);
                        InputEvent up = new InputEvent();
                        up.setType(InputEvent.Type.touchUp);
                        b.fire(up);
                    } catch (Exception ignored) {}
                }
            });
        } catch (Exception ignored) {}
    }
}

