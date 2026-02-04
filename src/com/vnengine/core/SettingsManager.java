package com.vnengine.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class SettingsManager {
    private static SettingsManager instance;
    private Properties props;
    private File settingsFile;

    private float musicVolume = 0.8f;
    private float sfxVolume = 1.0f;
    private float textSpeed = 0.5f; // 0.1 (slow) to 2.0 (fast)
    private boolean fullscreen = false;

    private SettingsManager() {
        props = new Properties();
        settingsFile = new File("settings.properties");
        load();
    }

    public static SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }

    public void load() {
        if (settingsFile.exists()) {
            try (FileInputStream in = new FileInputStream(settingsFile)) {
                props.load(in);
                musicVolume = Float.parseFloat(props.getProperty("musicVolume", "0.8"));
                sfxVolume = Float.parseFloat(props.getProperty("sfxVolume", "1.0"));
                textSpeed = Float.parseFloat(props.getProperty("textSpeed", "0.5"));
                fullscreen = Boolean.parseBoolean(props.getProperty("fullscreen", "false"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void save() {
        props.setProperty("musicVolume", String.valueOf(musicVolume));
        props.setProperty("sfxVolume", String.valueOf(sfxVolume));
        props.setProperty("textSpeed", String.valueOf(textSpeed));
        props.setProperty("fullscreen", String.valueOf(fullscreen));

        try (FileOutputStream out = new FileOutputStream(settingsFile)) {
            props.store(out, "VN Engine Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(float v) {
        this.musicVolume = Math.max(0f, Math.min(1f, v));
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    public void setSfxVolume(float v) {
        this.sfxVolume = Math.max(0f, Math.min(1f, v));
    }

    public float getTextSpeed() {
        return textSpeed;
    }

    public void setTextSpeed(float v) {
        this.textSpeed = v;
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public void setFullscreen(boolean v) {
        this.fullscreen = v;
    }
}
