package com.vnengine.core;

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
            System.out.println("Saved to slot " + slot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static SaveData load(int slot) {
        File f = new File(SAVE_DIR + "save" + slot + ".dat");
        if (!f.exists())
            return null;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (SaveData) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void delete(int slot) {
        File f = new File(SAVE_DIR + "save" + slot + ".dat");
        if (f.exists()) {
            f.delete();
            System.out.println("Deleted save in slot " + slot);
        }
    }
}
