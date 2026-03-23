package com.visualbean.core;

public class ProcessTerminator {

    public static void terminateProcess(String target) {
        try {
            ProcessHandle.allProcesses()
                    .filter(ph -> ph.info()
                            .command()
                            .map(cmd -> cmd.toLowerCase().endsWith(target.toLowerCase()))
                            .orElse(false))
                    .forEach(ph -> {
                        System.out.println("[ProcessTerminator] Closing: " +
                                ph.info().command().orElse("unknown"));
                        ph.destroy();
                    });
        } catch (Exception e) {
            System.err.println("[ProcessTerminator] Failed to terminate process: " + target);
            e.printStackTrace();
        }
    }

    public static void killWallpaperEngine() {
        terminateProcess("wallpaper32.exe");
        terminateProcess("wallpaper64.exe");
    }
}