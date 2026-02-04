package com.vnengine.tools;

import com.vnengine.core.SaveData;
import java.io.*;

public class SaveInspector {
    public static void main(String[] args) {
        System.out.println("--- Save Inspector v1.0 ---");
        File dir = new File("saves");
        if (!dir.exists()) {
            System.out.println("No 'saves' directory found in " + new File(".").getAbsolutePath());
            return;
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".dat"));
        if (files == null || files.length == 0) {
            System.out.println("No save files found.");
            return;
        }

        for (File f : files) {
            System.out.println(">>> Analyzing " + f.getName() + " <<<");
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                SaveData data = (SaveData) ois.readObject();
                System.out.println("  Timestamp : " + data.timestamp);
                System.out.println("  Step Index: " + data.stepIndex);
                System.out.println("  Snapshot  : \"" + data.snapshotText + "\"");

                System.out.println("  --- State ---");
                System.out.println("  Background: " + data.currentBackground);
                System.out.println("  Music     : " + data.currentMusic);
                if (data.windowSize != null) {
                    System.out.println("  Window Size: " + data.windowSize.width + "x" + data.windowSize.height);
                }
                if (data.windowPosition != null) {
                    System.out.println("  Window Pos : (" + data.windowPosition.x + ", " + data.windowPosition.y + ")");
                }
                if (data.dialogPosition != null) {
                    System.out.println("  Dialog Pos : (" + data.dialogPosition.x + ", " + data.dialogPosition.y + ")");
                } else {
                    System.out.println("  Dialog Pos : Default");
                }

                if (data.visibleCharacters != null && !data.visibleCharacters.isEmpty()) {
                    System.out.println("  Characters: ");
                    for (String name : data.visibleCharacters.keySet()) {
                        System.out.println("    - " + name + ": " + data.visibleCharacters.get(name));
                        if (data.characterPositions != null && data.characterPositions.containsKey(name)) {
                            java.awt.Point p = data.characterPositions.get(name);
                            System.out.println("      Pos  : (" + p.x + ", " + p.y + ")");
                        }
                        if (data.characterScales != null && data.characterScales.containsKey(name)) {
                            System.out.println("      Scale: " + data.characterScales.get(name));
                        }
                    }
                } else {
                    System.out.println("  Characters: None");
                }

                System.out.println("  File Size : " + f.length() + " bytes");
            } catch (Exception e) {
                System.out.println("  [ERROR] Could not read file: " + e.getMessage());
                e.printStackTrace();
            }
            System.out.println();
        }
    }
}
