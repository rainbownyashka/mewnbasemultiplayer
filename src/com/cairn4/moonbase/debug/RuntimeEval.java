package com.cairn4.moonbase.debug;

import com.badlogic.gdx.Gdx;
import com.cairn4.moonbase.MoonBase;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Dangerous runtime eval for local debugging only.
 * Enabled only when -Dmewnbase.eval=1 is set.
 */
public final class RuntimeEval {
    private static final AtomicBoolean started = new AtomicBoolean(false);
    private static volatile MoonBase context = null;

    public static void setContext(MoonBase mb) {
        context = mb;
    }

    public static MoonBase ctx() {
        return context;
    }

    public static void startIfEnabled() {
        String enabled = System.getProperty("mewnbase.eval");
        if (!"1".equals(enabled)) return;
        if (!started.compareAndSet(false, true)) return;

        String portProp = System.getProperty("mewnbase.eval.port", "8791");
        int portVal = 8791;
        try { portVal = Integer.parseInt(portProp); } catch (Exception ignored) {}
        final int port = portVal;
        final String token = System.getProperty("mewnbase.eval.token", "");

        try {
            Thread t = new Thread(() -> runServer(port, token), "MewnBase-RuntimeEval");
            t.setDaemon(true);
            t.start();
            try { Gdx.app.log("RuntimeEval", "Enabled on localhost:" + port + " (token required=" + (token.length() > 0) + ")"); } catch (Exception ignored) {}
        } catch (Exception e) {
            try { Gdx.app.error("RuntimeEval", "Failed to start eval server", e); } catch (Exception ignored) {}
        }
    }

    private static void runServer(int port, String token) {
        try (ServerSocket server = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"))) {
            Object jshell = newJShellOrNull();
            if (jshell == null) {
                try { Gdx.app.error("RuntimeEval", "JShell not available. Run with JDK 9+ and ensure jdk.jshell module exists.", null); } catch (Exception ignored) {}
                return;
            }
            // provide quick access to game context
            eval(jshell, "import com.cairn4.moonbase.*;");
            eval(jshell, "import com.cairn4.moonbase.debug.RuntimeEval;");
            eval(jshell, "var ctx = com.cairn4.moonbase.debug.RuntimeEval.ctx();");

            while (true) {
                try (Socket s = server.accept();
                     BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
                     PrintWriter pw = new PrintWriter(s.getOutputStream(), true)) {

                    String first = br.readLine();
                    if (first == null) continue;
                    if (token.length() > 0) {
                        if (!token.equals(first)) {
                            pw.println("ERR: bad token");
                            continue;
                        }
                    } else {
                        // no token configured, treat first line as code
                        String code = first;
                        String out = eval(jshell, code);
                        pw.println(out);
                        continue;
                    }

                    String code = br.readLine();
                    if (code == null) { pw.println("ERR: empty"); continue; }
                    String out = eval(jshell, code);
                    pw.println(out);
                } catch (Exception e) {
                    try { Gdx.app.error("RuntimeEval", "Eval session error", e); } catch (Exception ignored) {}
                }
            }
        } catch (Exception e) {
            try { Gdx.app.error("RuntimeEval", "Eval server failed", e); } catch (Exception ignored) {}
        }
    }

    private static Object newJShellOrNull() {
        try {
            Class<?> jshellCls = Class.forName("jdk.jshell.JShell");
            Object builder = jshellCls.getMethod("builder").invoke(null);
            return builder.getClass().getMethod("build").invoke(builder);
        } catch (Throwable t) {
            return null;
        }
    }

    private static String eval(Object jshell, String code) {
        StringBuilder sb = new StringBuilder();
        try {
            Class<?> jshellCls = jshell.getClass();
            @SuppressWarnings("unchecked")
            List<Object> events = (List<Object>) jshellCls.getMethod("eval", String.class).invoke(jshell, code);
            for (Object ev : events) {
                Object ex = ev.getClass().getMethod("exception").invoke(ev);
                Object val = ev.getClass().getMethod("value").invoke(ev);
                if (ex != null) {
                    sb.append("EX: ").append(ex.toString()).append("\n");
                } else if (val != null) {
                    sb.append(val).append("\n");
                } else {
                    sb.append("OK").append("\n");
                }
            }
        } catch (Exception e) {
            sb.append("EX: ").append(e.toString()).append("\n");
        }
        return sb.toString().trim();
    }
}
