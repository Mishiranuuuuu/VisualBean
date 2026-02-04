package com.vnengine.core;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AudioManager {
    private Clip currentMusic;
    private Map<String, Clip> sfxCache = new HashMap<>();

    private String currentMusicName;

    private float musicVolume = 0.8f;
    private float sfxVolume = 1.0f;

    public boolean isPlaying(String name) {
        return currentMusic != null && currentMusic.isRunning() &&
                name != null && name.equals(currentMusicName);
    }

    public void setMusicVolume(float volume) {
        this.musicVolume = volume;
        if (currentMusic != null) {
            setVolume(currentMusic, musicVolume);
        }
    }

    public void setSfxVolume(float volume) {
        this.sfxVolume = volume;
    }

    private void setVolume(Clip clip, float volume) {
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                // Convert 0.0-1.0 to Decibels
                // 1.0 -> 0 dB
                // 0.0 -> -80 dB (effectively mute)
                float dB;
                if (volume <= 0.0001f) {
                    dB = -80.0f;
                } else {
                    dB = (float) (Math.log10(volume) * 20.0);
                }

                // Clamp to supported range
                dB = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB));

                gainControl.setValue(dB);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playMusic(String name, boolean loop) {
        // If already playing this track, do nothing
        if (currentMusic != null && currentMusic.isRunning() && name.equals(currentMusicName)) {
            return;
        }

        stopMusic();
        try {
            File audioFile = findAudioFile("resources/audio/music/" + name);
            if (audioFile != null) {
                // Check for MP3
                if (audioFile.getName().toLowerCase().endsWith(".mp3")) {
                    javax.swing.JOptionPane.showMessageDialog(null,
                            "MP3 format is not supported by standard Java Sound.\nPlease convert '" + name
                                    + "' to WAV.",
                            "Audio Format Error", javax.swing.JOptionPane.WARNING_MESSAGE);
                    System.err.println("MP3 not supported: " + name);
                    return;
                }

                AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
                currentMusic = AudioSystem.getClip();
                currentMusic.open(audioStream);

                // Apply volume
                setVolume(currentMusic, musicVolume);

                if (loop) {
                    currentMusic.loop(Clip.LOOP_CONTINUOUSLY);
                } else {
                    currentMusic.start();
                }
                currentMusicName = name;
            } else {
                System.err.println("Music file not found: " + name);
            }
        } catch (UnsupportedAudioFileException e) {
            System.err.println("Unsupported Audio Format: " + name);
            javax.swing.JOptionPane.showMessageDialog(null,
                    "The audio file '" + name + "' is not supported.\nTry using standard WAV (16-bit PCM).",
                    "Audio Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopMusic() {
        if (currentMusic != null && currentMusic.isRunning()) {
            currentMusic.stop();
            currentMusic.close();
        }
        currentMusic = null;
        currentMusicName = null;
    }

    public void playSound(String name) {
        try {
            // optimizations: cache sfx clips? For now, load fresh just to be safe with
            // overlaps
            // or better, check cache.
            File audioFile = findAudioFile("resources/audio/sfx/" + name);
            if (audioFile != null) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);

                setVolume(clip, sfxVolume);

                clip.start();
            } else {
                System.err.println("SFX file not found: " + name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File findAudioFile(String basePath) {
        String[] extensions = { ".wav", ".au", ".aiff", ".mp3" }; // Added mp3 for detection
        File f = new File(basePath);
        if (f.exists())
            return f;

        for (String ext : extensions) {
            f = new File(basePath + ext);
            if (f.exists())
                return f;
        }
        return null;
    }
}
