package com.cairn4.moonbase.debug;

import com.badlogic.gdx.ApplicationLogger;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileLogger implements ApplicationLogger {
    private final PrintWriter out;
    private final SimpleDateFormat ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public FileLogger(String path) throws Exception {
        this.out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(path, true), StandardCharsets.UTF_8), true);
        this.out.println("---- log start " + ts.format(new Date()) + " ----");
    }

    private void write(String level, String tag, String message, Throwable exception) {
        String line = ts.format(new Date()) + " [" + level + "] " + tag + ": " + message;
        out.println(line);
        if (exception != null) {
            exception.printStackTrace(out);
        }
        out.flush();
    }

    @Override
    public void log(String tag, String message) {
        write("INFO", tag, message, null);
    }

    @Override
    public void log(String tag, String message, Throwable exception) {
        write("INFO", tag, message, exception);
    }

    @Override
    public void error(String tag, String message) {
        write("ERROR", tag, message, null);
    }

    @Override
    public void error(String tag, String message, Throwable exception) {
        write("ERROR", tag, message, exception);
    }

    @Override
    public void debug(String tag, String message) {
        write("DEBUG", tag, message, null);
    }

    @Override
    public void debug(String tag, String message, Throwable exception) {
        write("DEBUG", tag, message, exception);
    }
}
