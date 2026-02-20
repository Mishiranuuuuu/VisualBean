package com.visualbean.core;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SaveManager {
    private static final String SAVE_DIR = "saves/";

    static {
        new File(SAVE_DIR).mkdirs();
    }

    public static void save(int slot, SaveData data) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_DIR + "save" + slot + ".dat"))) {
            oos.writeObject(data);
            System.out.println("[SaveManager] Saved to slot " + slot);
        } catch (IOException e) {
            System.err.println("[SaveManager] Failed to save to slot " + slot);
            e.printStackTrace();
        }
    }

    public static SaveData load(int slot) {
        File f = new File(SAVE_DIR + "save" + slot + ".dat");
        if (!f.exists()) {
            System.err.println("[SaveManager] Save not found in slot " + slot);
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (SaveData) ois.readObject();
        } catch (Exception e) {
            System.err.println("[SaveManager] Failed to load from slot " + slot);
            e.printStackTrace();
            return null;
        }
    }

    public static void delete(int slot) {
        File f = new File(SAVE_DIR + "save" + slot + ".dat");
        if (f.exists()) {
            f.delete();
            System.out.println("[SaveManager] Deleted save in slot " + slot);
        }
    }
}
