package com.vnengine.script;

import com.vnengine.core.GameEngine;

public abstract class GameScript implements Runnable {
    protected GameEngine engine;

    public void setEngine(GameEngine engine) {
        this.engine = engine;
    }

    // --- DSL Methods ---

    protected void scene(String backgroundName) {
        engine.setBackground(backgroundName);
        if (!engine.isSkipping()) {
            sleep(100); // Small delay for effect
        }
    }

    protected void show(String characterName, String imageName) {
        engine.showCharacter(characterName, imageName);
    }

    protected void show(String characterName, String imageName, int x, int y) {
        engine.showCharacter(characterName, imageName, x, y);
    }

    protected void move(String characterName, int x, int y) {
        engine.moveCharacter(characterName, x, y);
    }

    protected void hide(String characterName) {
        engine.hideCharacter(characterName);
    }

    // Dialog Control
    protected void dialogPos(int x, int y) {
        engine.setDialogPosition(x, y);
    }

    protected void dialogReset() {
        engine.resetDialogPosition();
    }

    protected void say(String characterName, String text) {
        engine.say(characterName, text);
    }

    protected void narrator(String text) {
        engine.say(null, text);
    }

    protected int menu(String... options) {
        return engine.promptChoice(options);
    }

    protected void playMusic(String name) {
        engine.playMusic(name);
    }

    protected void stopMusic() {
        engine.stopMusic();
    }

    protected void playSound(String name) {
        engine.playSound(name);
    }

    protected void music(String name) {
        playMusic(name);
    }

    protected void sound(String name) {
        playSound(name);
    }

    // --- Meta Features ---
    protected void windowMove(int x, int y) {
        engine.setWindowPosition(x, y);
    }

    protected void windowCenter() {
        engine.centerWindow();
    }

    protected void windowCenter(int duration) {
        windowCenter(duration, "EASE_IN_OUT_CUBIC");
    }

    protected void windowCenter(int duration, String easing) {
        try {
            com.vnengine.util.Easing e = com.vnengine.util.Easing.valueOf(easing.toUpperCase());
            engine.centerWindow(duration, e);
        } catch (IllegalArgumentException ex) {
            System.err.println("Invalid easing: " + easing + ", using default.");
            engine.centerWindow(duration, com.vnengine.util.Easing.EASE_IN_OUT_CUBIC);
        }
    }

    protected void windowShake(int intensity, int duration) {
        engine.shakeWindow(intensity, duration);
    }

    protected void windowSlide(int x, int y, int duration) {
        engine.slideWindow(x, y, duration);
    }

    protected void windowSlide(int x, int y, int duration, String easing) {
        try {
            com.vnengine.util.Easing e = com.vnengine.util.Easing.valueOf(easing.toUpperCase());
            engine.slideWindow(x, y, duration, e);
        } catch (IllegalArgumentException ex) {
            System.err.println("Invalid easing: " + easing + ", using default.");
            engine.slideWindow(x, y, duration);
        }
    }

    protected void windowSize(int width, int height) {
        engine.setWindowSize(width, height);
    }

    protected void windowTitle(String title) {
        engine.setWindowTitle(title);
    }

    protected void windowFullscreen(boolean fullscreen) {
        engine.setFullscreen(fullscreen);
    }

    // Character Scaling
    protected void show(String characterName, String imageName, double scale) {
        engine.showCharacter(characterName, imageName, -1, -1, scale);
    }

    protected void show(String characterName, String imageName, int x, int y, double scale) {
        engine.showCharacter(characterName, imageName, x, y, scale);
    }

    protected void scale(String name, double factor) {
        engine.setCharacterScale(name, factor);
    }

    // Animated Entity Moves
    protected void move(String name, int x, int y, int duration) {
        move(name, x, y, duration, "EASE_IN_OUT_CUBIC");
    }

    protected void move(String name, int x, int y, int duration, String easing) {
        try {
            com.vnengine.util.Easing e = com.vnengine.util.Easing.valueOf(easing.toUpperCase());
            engine.slideCharacter(name, x, y, duration, e);
        } catch (IllegalArgumentException ex) {
            System.err.println("Invalid easing: " + easing + ", using default.");
            engine.slideCharacter(name, x, y, duration, com.vnengine.util.Easing.EASE_IN_OUT_CUBIC);
        }
    }

    protected void dialogSlide(int x, int y, int duration) {
        dialogSlide(x, y, duration, "EASE_IN_OUT_CUBIC");
    }

    protected void dialogSlide(int x, int y, int duration, String easing) {
        try {
            com.vnengine.util.Easing e = com.vnengine.util.Easing.valueOf(easing.toUpperCase());
            engine.slideDialog(x, y, duration, e);
        } catch (IllegalArgumentException ex) {
            System.err.println("Invalid easing: " + easing + ", using default.");
            engine.slideDialog(x, y, duration, com.vnengine.util.Easing.EASE_IN_OUT_CUBIC);
        }
    }

    protected void resizeWindow(int width, int height, int duration) {
        resizeWindow(width, height, duration, "EASE_IN_OUT_CUBIC");
    }

    protected void resizeWindow(int width, int height, int duration, String easing) {
        try {
            com.vnengine.util.Easing e = com.vnengine.util.Easing.valueOf(easing.toUpperCase());
            engine.resizeWindow(width, height, duration, e);
        } catch (IllegalArgumentException ex) {
            System.err.println("Invalid easing: " + easing + ", using default.");
            engine.resizeWindow(width, height, duration, com.vnengine.util.Easing.EASE_IN_OUT_CUBIC);
        }
    }

    protected void resizeWindowCentered(int width, int height, int duration) {
        resizeWindowCentered(width, height, duration, "EASE_IN_OUT_CUBIC");
    }

    protected void resizeWindowCentered(int width, int height, int duration, String easing) {
        try {
            com.vnengine.util.Easing e = com.vnengine.util.Easing.valueOf(easing.toUpperCase());
            engine.resizeWindow(width, height, duration, e, true);
        } catch (IllegalArgumentException ex) {
            System.err.println("Invalid easing: " + easing + ", using default.");
            engine.resizeWindow(width, height, duration, com.vnengine.util.Easing.EASE_IN_OUT_CUBIC, true);
        }
    }

    protected void scale(String name, double scale, int duration) {
        scale(name, scale, duration, "EASE_IN_OUT_CUBIC");
    }

    protected void scale(String name, double scale, int duration, String easing) {
        try {
            com.vnengine.util.Easing e = com.vnengine.util.Easing.valueOf(easing.toUpperCase());
            engine.scaleCharacter(name, scale, duration, e);
        } catch (IllegalArgumentException ex) {
            System.err.println("Invalid easing: " + easing + ", using default.");
            engine.scaleCharacter(name, scale, duration, com.vnengine.util.Easing.EASE_IN_OUT_CUBIC);
        }
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
        }
    }

    // Fake Error
    protected void fakeError(String title, String message) {
        engine.showFakeError(title, message, -1, -1);
    }

    protected void fakeError(String title, String message, int x, int y) {
        engine.showFakeError(title, message, x, y);
    }

    protected void clearFakeError() {
        engine.clearFakeError();
    }

    // --- Sub-Window Control ---
    protected void createSubWindow(String id, String title, int width, int height) {
        engine.createSubWindow(id, title, width, height);
    }

    protected void closeSubWindow(String id) {
        engine.closeSubWindow(id);
    }

    protected void subWindowPos(String id, int x, int y) {
        engine.setSubWindowPosition(id, x, y);
    }

    protected void subWindowBg(String id, String imagePath) {
        engine.setSubWindowBackground(id, imagePath);
    }

    protected void showInSubWindow(String id, String name, String image, int x, int y) {
        engine.showCharacterInSubWindow(id, name, image, x, y);
    }

    protected void showInSubWindow(String id, String name, String image, int x, int y, double scale) {
        engine.showCharacterInSubWindow(id, name, image, x, y, scale);
    }

    protected void moveInSubWindow(String id, String name, int x, int y) {
        engine.moveCharacterInSubWindow(id, name, x, y);
    }

    protected void subWindowSay(String id, String name, String text) {
        // Since SubWindows don't halt the main script for input (they are parallel),
        // we just update the text.
        engine.subWindowSay(id, name, text);
    }

    protected void website(String url) {
        engine.openWebsite(url);
    }

    protected void openWeb(String url) {
        engine.openWebsite(url);
    }

    @Override
    public abstract void run();
}
