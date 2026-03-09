package com.visualbean.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Writes crash/error logs with full stack traces to a log file
 * in the game's root directory.
 */
public class CrashLogger {

    private static final String LOG_DIR = "logs";
    private static final DateTimeFormatter FILE_DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Logs a throwable with full stack trace to a date-stamped log file.
     *
     * @param context a short description of where the error occurred
     *                (e.g. "Script execution", "Main entry point")
     * @param t       the throwable to log
     */
    public static void log(String context, Throwable t) {
        try {
            File logDir = new File(LOG_DIR);
            if (!logDir.exists()) {
                logDir.mkdirs();
            }

            String date = LocalDateTime.now().format(FILE_DATE_FMT);
            File logFile = new File(logDir, "crash-" + date + ".log");

            try (FileWriter fw = new FileWriter(logFile, true);
                    PrintWriter pw = new PrintWriter(fw)) {

                pw.println("=".repeat(70));
                pw.println("[" + LocalDateTime.now().format(TIMESTAMP_FMT) + "] ERROR in: " + context);
                pw.println("-".repeat(70));
                pw.println("Exception: " + t.getClass().getName());
                pw.println("Message:   " + t.getMessage());
                pw.println();
                pw.println("Stack Trace:");
                t.printStackTrace(pw);

                // Print the full cause chain
                Throwable cause = t.getCause();
                while (cause != null) {
                    pw.println();
                    pw.println("Caused by: " + cause.getClass().getName() + ": " + cause.getMessage());
                    cause.printStackTrace(pw);
                    cause = cause.getCause();
                }

                pw.println("=".repeat(70));
                pw.println();
            }

            System.err.println("[CrashLogger] Error logged to: " + logFile.getAbsolutePath());

        } catch (IOException ioe) {
            System.err.println("[CrashLogger] Failed to write log file: " + ioe.getMessage());
            System.err.println("[CrashLogger] Original error (" + context + "):");
            t.printStackTrace();
        }
    }

    /**
     * Installs a global uncaught exception handler that logs any unhandled
     * exceptions from any thread.
     */
    public static void installGlobalHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            log("Uncaught exception in thread: " + thread.getName(), throwable);
        });
    }
}
