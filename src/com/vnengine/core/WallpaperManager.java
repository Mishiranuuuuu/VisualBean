package com.vnengine.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class WallpaperManager {

    private static String savedWallpaperPath = null;
    private static boolean isWindows = System.getProperty("os.name", "").toLowerCase().contains("win");

    public static String getCurrentWallpaper() {
        if (!isWindows) {
            System.err.println("[WallpaperManager] Wallpaper operations are only supported on Windows.");
            return null;
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "powershell", "-Command",
                    "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8; " +
                            "(Get-ItemProperty -Path 'HKCU:\\Control Panel\\Desktop' -Name Wallpaper).Wallpaper");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line.trim());
            }
            process.waitFor();

            String path = output.toString().trim();
            if (!path.isEmpty()) {
                System.out.println("[WallpaperManager] Current wallpaper: " + path);
                return path;
            }
        } catch (Exception e) {
            System.err.println("[WallpaperManager] Error getting current wallpaper: " + e.getMessage());
        }
        return null;
    }

    public static void saveCurrentWallpaper() {
        if (savedWallpaperPath == null) {
            savedWallpaperPath = getCurrentWallpaper();
            if (savedWallpaperPath != null) {
                System.out.println("[WallpaperManager] Saved wallpaper for later restore: " + savedWallpaperPath);
            }
        }
    }

    public static boolean setWallpaper(String imagePath) {
        if (!isWindows) {
            System.err.println("[WallpaperManager] Wallpaper operations are only supported on Windows.");
            return false;
        }

        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            System.err.println("[WallpaperManager] Image file not found: " + imagePath);
            return false;
        }

        String absolutePath = imageFile.getAbsolutePath();

        File tempScript = null;
        try {
            tempScript = File.createTempFile("vnengine_wallpaper_", ".ps1");
            tempScript.deleteOnExit();

            try (FileWriter writer = new FileWriter(tempScript, StandardCharsets.UTF_8)) {
                writer.write('\uFEFF');
                writer.write("Add-Type -TypeDefinition @\"\n");
                writer.write("using System;\n");
                writer.write("using System.Runtime.InteropServices;\n");
                writer.write("public class WallpaperSetter {\n");
                writer.write("    [DllImport(\"user32.dll\", CharSet = CharSet.Unicode)]\n");
                writer.write(
                        "    public static extern int SystemParametersInfo(int uAction, int uParam, string lpvParam, int fuWinIni);\n");
                writer.write("}\n");
                writer.write("\"@\n");
                writer.write("\n");
                writer.write("[WallpaperSetter]::SystemParametersInfo(0x0014, 0, '"
                        + absolutePath.replace("'", "''") + "', 0x01 -bor 0x02)\n");
            }

            ProcessBuilder pb = new ProcessBuilder(
                    "powershell", "-ExecutionPolicy", "Bypass", "-File", tempScript.getAbsolutePath());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[WallpaperManager] PS: " + line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("[WallpaperManager] Wallpaper changed to: " + absolutePath);
                return true;
            } else {
                System.err.println("[WallpaperManager] PowerShell exited with code: " + exitCode);
            }
        } catch (Exception e) {
            System.err.println("[WallpaperManager] Error setting wallpaper: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (tempScript != null && tempScript.exists()) {
                tempScript.delete();
            }
        }
        return false;
    }

    public static boolean restoreWallpaper() {
        if (savedWallpaperPath == null) {
            System.err.println("[WallpaperManager] No saved wallpaper to restore.");
            return false;
        }

        File savedFile = new File(savedWallpaperPath);
        if (!savedFile.exists()) {
            System.err.println("[WallpaperManager] Saved wallpaper file no longer exists: " + savedWallpaperPath);
            System.err.println("[WallpaperManager] This may be a Unicode path issue. Attempting restore anyway...");
        }

        System.out.println("[WallpaperManager] Restoring wallpaper to: " + savedWallpaperPath);
        boolean success = setWallpaperByPath(savedWallpaperPath);
        if (success) {
            savedWallpaperPath = null;
        }
        return success;
    }

    private static boolean setWallpaperByPath(String absolutePath) {
        if (!isWindows) {
            return false;
        }

        File tempScript = null;
        try {
            tempScript = File.createTempFile("vnengine_wallpaper_", ".ps1");
            tempScript.deleteOnExit();

            try (FileWriter writer = new FileWriter(tempScript, StandardCharsets.UTF_8)) {
                writer.write('\uFEFF');
                writer.write("Add-Type -TypeDefinition @\"\n");
                writer.write("using System;\n");
                writer.write("using System.Runtime.InteropServices;\n");
                writer.write("public class WallpaperSetter {\n");
                writer.write("    [DllImport(\"user32.dll\", CharSet = CharSet.Unicode)]\n");
                writer.write(
                        "    public static extern int SystemParametersInfo(int uAction, int uParam, string lpvParam, int fuWinIni);\n");
                writer.write("}\n");
                writer.write("\"@\n");
                writer.write("\n");
                writer.write("[WallpaperSetter]::SystemParametersInfo(0x0014, 0, '"
                        + absolutePath.replace("'", "''") + "', 0x01 -bor 0x02)\n");
            }

            ProcessBuilder pb = new ProcessBuilder(
                    "powershell", "-ExecutionPolicy", "Bypass", "-File", tempScript.getAbsolutePath());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[WallpaperManager] PS: " + line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("[WallpaperManager] Wallpaper restored to: " + absolutePath);
                return true;
            } else {
                System.err.println("[WallpaperManager] PowerShell exited with code: " + exitCode);
            }
        } catch (Exception e) {
            System.err.println("[WallpaperManager] Error restoring wallpaper: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (tempScript != null && tempScript.exists()) {
                tempScript.delete();
            }
        }
        return false;
    }

    public static String getSavedWallpaperPath() {
        return savedWallpaperPath;
    }

    public static boolean isSupported() {
        return isWindows;
    }
}
