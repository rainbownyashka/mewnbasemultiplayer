package com.cairn4.moonbase.debug;

import com.badlogic.gdx.Gdx;
import com.cairn4.moonbase.MoonBase;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.JavaFileManager;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.StandardJavaFileManager;
import javax.tools.DiagnosticCollector;
import javax.tools.Diagnostic;
import javax.tools.JavaCompiler.CompilationTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Dangerous runtime eval for local debugging only.
 * Enabled only when -Dmewnbase.eval=1 is set.
 */
public final class RuntimeEval {
    private static final AtomicBoolean started = new AtomicBoolean(false);
    private static volatile MoonBase context = null;
    private static final AtomicInteger evalSeq = new AtomicInteger(1);

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
            if (jshell != null) {
                // provide quick access to game context (JShell fallback)
                eval(jshell, "import com.cairn4.moonbase.*;");
                eval(jshell, "import com.cairn4.moonbase.debug.RuntimeEval;");
                eval(jshell, "var ctx = com.cairn4.moonbase.debug.RuntimeEval.ctx();");
            }

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
                        String direct = handleDirect(code);
                        if (direct != null) {
                            pw.println(direct);
                            continue;
                        }
                        String out = evalJava(code);
                        if (out == null && jshell != null) out = eval(jshell, code);
                        if (out == null) out = "ERR: no_eval_engine";
                        pw.println(out);
                        continue;
                    }

                    String code = br.readLine();
                    if (code == null) { pw.println("ERR: empty"); continue; }
                    String direct = handleDirect(code);
                    if (direct != null) { pw.println(direct); continue; }
                    String out = evalJava(code);
                    if (out == null && jshell != null) out = eval(jshell, code);
                    if (out == null) out = "ERR: no_eval_engine";
                    pw.println(out);
                } catch (Exception e) {
                    try { Gdx.app.error("RuntimeEval", "Eval session error", e); } catch (Exception ignored) {}
                }
            }
        } catch (Exception e) {
            try { Gdx.app.error("RuntimeEval", "Eval server failed", e); } catch (Exception ignored) {}
        }
    }

    private static String handleDirect(String code) {
        if (code == null) return "ERR: empty";
        String cmd = code.trim();
        if (cmd.length() == 0) return "ERR: empty";
        if ("PING".equalsIgnoreCase(cmd)) return "OK";
        if (cmd.toUpperCase().startsWith("SPAWN ")) {
            String type = cmd.substring("SPAWN ".length()).trim().toLowerCase();
            if (type.length() == 0) return "ERR: spawn_type_required";
            try {
                com.cairn4.moonbase.MoonBase mb = com.cairn4.moonbase.MoonBase.instance;
                if (mb == null || mb.getScreen() == null || !(mb.getScreen() instanceof com.cairn4.moonbase.ui.GameScreen)) {
                    return "ERR: no_game";
                }
                com.cairn4.moonbase.ui.GameScreen gs = (com.cairn4.moonbase.ui.GameScreen) mb.getScreen();
                com.cairn4.moonbase.World w = gs.world;
                float x = w.player.getXPos();
                float y = w.player.getYPos() - 200.0f;
                float rot = -90.0f;
                com.cairn4.moonbase.entities.Entity ent = null;
                if ("buggie".equals(type)) {
                    ent = new com.cairn4.moonbase.entities.Buggie(w, x, y, rot);
                } else if ("tank".equals(type)) {
                    ent = new com.cairn4.moonbase.entities.Tank(w, x, y, rot);
                } else if ("trailer".equals(type)) {
                    ent = new com.cairn4.moonbase.entities.VehicleTrailer(w, x, y, rot);
                } else {
                    return "ERR: unknown_spawn_type";
                }
                long id = w.getEntityId();
                w.registerNetworkEntity(ent, id);
                try {
                    if (ent instanceof com.cairn4.moonbase.entities.Vehicle) {
                        ((com.cairn4.moonbase.entities.Vehicle)ent).spawnAnim();
                    }
                } catch (Exception ignored) {}
                try {
                    com.cairn4.moonbase.Server s = com.cairn4.moonbase.Server.getActiveServer();
                    if (s != null) {
                        String payload = "ENTITY_SPAWN:" + type + ":" + id + ":" + x + ":" + y + ":" + rot;
                        s.broadcastFromServer(payload);
                    }
                } catch (Exception ignored) {}
                return "OK";
            } catch (Exception e) {
                return "ERR: " + e.getClass().getSimpleName();
            }
        }
        return null;
    }

    private static Object newJShellOrNull() {
        try {
            Class<?> jshellCls = Class.forName("jdk.jshell.JShell");
            Object builder = jshellCls.getMethod("builder").invoke(null);
            // Ensure JShell sees the game classes when running from -jar.
            try {
                String cp = System.getProperty("java.class.path", "");
                if (cp != null && cp.length() > 0) {
                    try {
                        builder.getClass().getMethod("compilerOptions", String[].class)
                                .invoke(builder, (Object)new String[]{"--class-path", cp});
                    } catch (Exception ignored) {}
                    try {
                        builder.getClass().getMethod("remoteVMOptions", String[].class)
                                .invoke(builder, (Object)new String[]{"--class-path", cp});
                    } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}
            return builder.getClass().getMethod("build").invoke(builder);
        } catch (Throwable t) {
            return null;
        }
    }

    private static String evalJava(String code) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            return null;
        }
        if (code == null) return "ERR: empty";
        String body = code.trim();
        if (body.length() == 0) return "ERR: empty";

        boolean hasReturn = body.contains("return ");
        if (!hasReturn) {
            if (!body.endsWith(";")) body = body + ";";
            body = body + " return \"OK\";";
        }

        int id = evalSeq.getAndIncrement();
        String className = "EvalSnippet" + id;
        String fullName = "com.cairn4.moonbase.debug.eval." + className;

        StringBuilder src = new StringBuilder();
        src.append("package com.cairn4.moonbase.debug.eval;\n");
        src.append("import com.cairn4.moonbase.*;\n");
        src.append("import com.cairn4.moonbase.ui.*;\n");
        src.append("import com.cairn4.moonbase.entities.*;\n");
        src.append("public class ").append(className).append(" {\n");
        src.append("  public static String run() throws Exception {\n");
        src.append("    com.cairn4.moonbase.MoonBase ctx = com.cairn4.moonbase.MoonBase.instance;\n");
        src.append("    com.cairn4.moonbase.ui.GameScreen gs = null;\n");
        src.append("    if (ctx != null && ctx.getScreen() instanceof com.cairn4.moonbase.ui.GameScreen) {\n");
        src.append("      gs = (com.cairn4.moonbase.ui.GameScreen)ctx.getScreen();\n");
        src.append("    }\n");
        src.append("    ").append(body).append("\n");
        src.append("  }\n");
        src.append("}\n");

        DiagnosticCollector<JavaFileObject> diags = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager std = compiler.getStandardFileManager(diags, null, null);
        MemoryFileManager fm = new MemoryFileManager(std);
        MemorySource source = new MemorySource(fullName, src.toString());

        String cp = System.getProperty("java.class.path", "");
        java.util.List<String> opts = java.util.Arrays.asList("-classpath", cp, "-source", "1.8", "-target", "1.8");
        CompilationTask task = compiler.getTask(null, fm, diags, opts, null,
                java.util.Collections.singletonList(source));
        boolean ok = false;
        try {
            ok = task.call();
        } catch (Exception e) {
            return "ERR: compile_exception";
        }
        if (!ok) {
            StringBuilder sb = new StringBuilder("ERR: compile\n");
            for (Diagnostic<? extends JavaFileObject> d : diags.getDiagnostics()) {
                sb.append(d.getKind()).append(": ").append(d.getMessage(null)).append("\n");
            }
            return sb.toString().trim();
        }

        try {
            MemoryClassLoader loader = new MemoryClassLoader(fm.getAllClassBytes(), RuntimeEval.class.getClassLoader());
            Class<?> cls = loader.loadClass(fullName);
            Object out = cls.getMethod("run").invoke(null);
            return out == null ? "null" : String.valueOf(out);
        } catch (Exception e) {
            return "ERR: run_exception";
        }
    }

    private static final class MemorySource extends SimpleJavaFileObject {
        private final String code;
        MemorySource(String className, String code) {
            super(URI.create("string:///" + className.replace('.', '/') + JavaFileObject.Kind.SOURCE.extension),
                  JavaFileObject.Kind.SOURCE);
            this.code = code;
        }
        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }

    private static final class MemoryByteCode extends SimpleJavaFileObject {
        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MemoryByteCode(String className) {
            super(URI.create("bytes:///" + className.replace('.', '/') + JavaFileObject.Kind.CLASS.extension),
                  JavaFileObject.Kind.CLASS);
        }
        @Override
        public OutputStream openOutputStream() throws IOException {
            return baos;
        }
        byte[] getBytes() { return baos.toByteArray(); }
    }

    private static final class MemoryFileManager extends ForwardingJavaFileManager<JavaFileManager> {
        private final Map<String, MemoryByteCode> classBytes = new HashMap<String, MemoryByteCode>();
        MemoryFileManager(JavaFileManager fileManager) { super(fileManager); }
        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className,
                                                   JavaFileObject.Kind kind, FileObject sibling)
                throws IOException {
            MemoryByteCode mbc = new MemoryByteCode(className);
            classBytes.put(className, mbc);
            return mbc;
        }
        Map<String, byte[]> getAllClassBytes() {
            Map<String, byte[]> out = new HashMap<String, byte[]>();
            for (Map.Entry<String, MemoryByteCode> e : classBytes.entrySet()) {
                out.put(e.getKey(), e.getValue().getBytes());
            }
            return out;
        }
    }

    private static final class MemoryClassLoader extends ClassLoader {
        private final Map<String, byte[]> classBytes;
        MemoryClassLoader(Map<String, byte[]> classBytes, ClassLoader parent) {
            super(parent);
            this.classBytes = classBytes;
        }
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] buf = classBytes.get(name);
            if (buf != null) {
                return defineClass(name, buf, 0, buf.length);
            }
            return super.findClass(name);
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
