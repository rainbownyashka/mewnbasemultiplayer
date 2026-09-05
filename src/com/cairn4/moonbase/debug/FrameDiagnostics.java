package com.cairn4.moonbase.debug;

import com.badlogic.gdx.Gdx;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/** Render-thread-only, opt-in timing; no per-frame output or GL synchronization. */
public final class FrameDiagnostics {
    public static final boolean ENABLED = Boolean.getBoolean("mewnbase.perf");
    private static final double[] frames = new double[120];
    private static final Map<String, double[]> stages = new LinkedHashMap<String, double[]>();
    private static int count;
    private static long frameStart;
    private static long previousFrameStart;
    private static double frameInterval;
    private static long lastSlowLog;
    private FrameDiagnostics() {}

    public static long start() { return ENABLED ? System.nanoTime() : 0L; }
    public static void beginFrame() {
        if (!ENABLED) return;
        frameStart = System.nanoTime();
        frameInterval = previousFrameStart == 0L ? 0 : (frameStart - previousFrameStart) / 1000000.0;
        previousFrameStart = frameStart;
    }
    public static void record(String name, long started) {
        if (!ENABLED) return;
        double ms = (System.nanoTime() - started) / 1000000.0;
        double[] stat = stages.get(name);
        if (stat == null) { stat = new double[3]; stages.put(name, stat); }
        stat[0] += ms;
        stat[1] = Math.max(stat[1], ms);
        stat[2]++;
    }

    public static void endFrame(String state) {
        if (!ENABLED) return;
        long now = System.nanoTime();
        record("render", frameStart);
        double ms = frameInterval > 0 ? frameInterval : (now - frameStart) / 1000000.0;
        frames[count++] = ms;
        if (ms >= 250 && now - lastSlowLog > 1000000000L) {
            lastSlowLog = now;
            Gdx.app.log("FrameDiagnostics", "stage=frame result=slow ms=" + ms + " " + state);
        }
        if (count < frames.length) return;
        double[] sorted = frames.clone();
        Arrays.sort(sorted);
        StringBuilder report = new StringBuilder("stage=summary frames=120 medianMs=")
                .append(sorted[60]).append(" p95Ms=").append(sorted[113])
                .append(" maxMs=").append(sorted[119]).append(' ').append(state);
        for (Map.Entry<String, double[]> e : stages.entrySet()) {
            double[] s = e.getValue();
            report.append(' ').append(e.getKey()).append("Mean/MaxMs=")
                    .append(s[0] / s[2]).append('/').append(s[1]);
        }
        Gdx.app.log("FrameDiagnostics", report.toString());
        count = 0;
        stages.clear();
    }
}
