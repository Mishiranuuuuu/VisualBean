package com.visualbean.core;

import com.visualbean.ui.GameWindow;
import com.visualbean.ui.SubWindow;
import java.awt.Dimension;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;

public class GameEngine {
    private GameWindow window;
    private Map<String, SubWindow> subWindows = new HashMap<>();
    private String currentBackground;
    private Map<String, String> visibleCharacters;
    private Map<String, Double> characterScales;
    private Map<String, Point> characterPositions;
    private Point customDialogPosition = null;
    private String currentWindowTitle = "VisualBean";

    private String currentSpeaker;
    private String currentDialogue;

    private AudioManager audioManager;

    private volatile boolean waitingForClick = false;
    private volatile boolean scriptCancelled = false;
    private volatile boolean autoMode = false;

    private Map<String, Thread> characterAnimations = new HashMap<>();
    private Map<String, Thread> characterScaleAnimations = new HashMap<>();
    private Thread windowAnimation = null;
    private Thread windowResizeAnimation = null;
    private Thread dialogAnimation = null;

    public GameEngine() {
        this.visibleCharacters = new HashMap<>();
        this.characterPositions = new HashMap<>();
        this.characterScales = new HashMap<>();
        this.audioManager = new AudioManager();
        this.window = new GameWindow(this);
        applySettings();
    }

    public void applySettings() {
        SettingsManager settings = SettingsManager.getInstance();
        audioManager.setMusicVolume(settings.getMusicVolume());
        audioManager.setSfxVolume(settings.getSfxVolume());
        if (window != null) {
            window.applySettings();
        }
    }

    // Dialogue history
    public static class LogEntry {
        public String speaker;
        public String text;

        public LogEntry(String s, String t) {
            speaker = s;
            text = t;
        }
    }

    private java.util.List<LogEntry> backlog = new java.util.ArrayList<>();

    public void setWindowSize(int width, int height) {
        if (isSkipping())
            return;
        SwingUtilities.invokeLater(() -> {
            window.setSize(width, height);
            window.setLocationRelativeTo(null);
            window.repaint();
        });

    }

    public void setFullscreen(boolean fullscreen) {
        if (isSkipping())
            return;
        SwingUtilities.invokeLater(() -> {
            window.setFullscreen(fullscreen);
        });
    }

    public void setWindowTitle(String title) {
        if (isSkipping())
            return;
        this.currentWindowTitle = title;
        SwingUtilities.invokeLater(() -> {
            window.setTitle(title);
        });
    }

    public void setBackground(String imagePath) {
        if (isSkipping())
            return;
        this.currentBackground = imagePath;
        window.repaint();
    }

    public void showCharacter(String name, String imagePath) {
        showCharacter(name, imagePath, -1, -1, 1.0);
    }

    public void showCharacter(String name, String imagePath, int x, int y) {
        showCharacter(name, imagePath, x, y, 1.0);
    }

    public void showCharacter(String name, String imagePath, int x, int y, double scale) {
        if (isSkipping())
            return;

        visibleCharacters.put(name, imagePath);
        if (x != -1 && y != -1) {
            characterPositions.put(name, new Point(x, y));
        }
        characterScales.put(name, scale);
        window.repaint();
    }

    public void setCharacterScale(String name, double scale) {
        if (isSkipping())
            return;

        if (visibleCharacters.containsKey(name)) {
            characterScales.put(name, scale);
            window.repaint();
        }
    }

    public void start() {
        SwingUtilities.invokeLater(() -> {
            window.setVisible(true);
        });
    }

    public void moveCharacter(String name, int x, int y) {
        if (isSkipping())
            return;

        if (visibleCharacters.containsKey(name)) {
            characterPositions.put(name, new Point(x, y));
            window.repaint();
        }
    }

    public void hideCharacter(String name) {
        if (isSkipping())
            return;

        visibleCharacters.remove(name);
        characterPositions.remove(name);
        characterScales.remove(name);
        window.repaint();
    }

    public void setDialogPosition(int x, int y) {
        if (isSkipping())
            return;
        this.customDialogPosition = new Point(x, y);
        window.repaint();
    }

    public void resetDialogPosition() {
        if (isSkipping())
            return;
        this.customDialogPosition = null;
        window.repaint();
    }

    // Track intended music to restore it after skip-mode
    private String intendedMusic = null;

    public void playMusic(String name) {
        playMusic(name, true);
    }

    public void playMusic(String name, boolean loop) {
        this.intendedMusic = name;
        if (!isSkipping()) {
            audioManager.playMusic(name, loop);
        }
    }

    public void stopMusic() {
        this.intendedMusic = null;
        if (!isSkipping()) {
            audioManager.stopMusic();
        }
    }

    public void playSound(String name) {
        if (!isSkipping()) {
            audioManager.playSound(name);
        }
    }

    public Point getCharacterPosition(String name) {
        return characterPositions.get(name);
    }

    public double getCharacterScale(String name) {
        return characterScales.getOrDefault(name, 1.0);
    }

    public Point getCustomDialogPosition() {
        return customDialogPosition;
    }

    public String getCurrentBackground() {
        return currentBackground;
    }

    public Map<String, String> getVisibleCharacters() {
        return visibleCharacters;
    }

    public String getCurrentSpeaker() {
        return currentSpeaker;
    }

    public String getCurrentDialogue() {
        return currentDialogue;
    }

    public String[] getCurrentOptions() {
        return currentOptions;
    }

    // Native error popup system
    public static class FakeError {
        public String title;
        public String message;
        public int x;
        public int y;

        public FakeError(String title, String message, int x, int y) {
            this.title = title;
            this.message = message;
            this.x = x;
            this.y = y;
        }
    }

    private javax.swing.JDialog currentErrorDialog = null;

    public void showFakeError(String title, String message, int x, int y) {
        if (isSkipping())
            return;

        SwingUtilities.invokeLater(() -> {
            // Close existing popup if any
            if (currentErrorDialog != null && currentErrorDialog.isVisible()) {
                currentErrorDialog.dispose();
            }

            javax.swing.JOptionPane optionPane = new javax.swing.JOptionPane(message,
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            currentErrorDialog = optionPane.createDialog(window, title);
            currentErrorDialog.setModal(false);

            if (x != -1 && y != -1) {
                try {
                    Point pLoc = window.getContentPane().getLocationOnScreen();
                    currentErrorDialog.setLocation(pLoc.x + x, pLoc.y + y);
                } catch (Exception e) {
                    currentErrorDialog.setLocationRelativeTo(window);
                }
            } else {
                currentErrorDialog.setLocationRelativeTo(window);
            }

            currentErrorDialog.setVisible(true);
        });
    }

    public void clearFakeError() {
        if (isSkipping())
            return;

        SwingUtilities.invokeLater(() -> {
            if (currentErrorDialog != null) {
                currentErrorDialog.dispose();
                currentErrorDialog = null;
            }
        });
    }

    public FakeError getFakeError() {
        return null;
    }

    // Save/Load system
    private int currentStep = 0;
    private int targetStep = -1;
    private Runnable currentScript;
    private Thread scriptThread;

    public void saveGame(int slot) {
        String desc = (currentSpeaker != null ? currentSpeaker + ": " : "") +
                (currentDialogue != null ? currentDialogue : "...");
        if (desc.length() > 50)
            desc = desc.substring(0, 47) + "...";

        SaveData data = new SaveData(currentStep, desc);

        // Capture visual state snapshot
        data.currentBackground = this.currentBackground;
        data.visibleCharacters = new HashMap<>(this.visibleCharacters);
        data.characterPositions = new HashMap<>(this.characterPositions);
        data.characterScales = new HashMap<>(this.characterScales);
        data.currentMusic = this.intendedMusic;
        data.windowSize = window.getSize();
        data.windowPosition = window.getLocation();
        data.windowTitle = this.currentWindowTitle;
        data.dialogPosition = this.customDialogPosition;

        SaveManager.save(slot, data);
    }

    public void loadGame(int slot) {
        SaveData data = SaveManager.load(slot);
        if (data == null) {
            System.err.println("No save data in slot " + slot);
            return;
        }

        // Restore visual state immediately so the player sees the saved scene
        this.currentStep = 0;
        this.targetStep = data.stepIndex;

        this.currentBackground = data.currentBackground;
        this.visibleCharacters = new HashMap<>(data.visibleCharacters);
        this.characterPositions = new HashMap<>(data.characterPositions);
        this.characterScales = new HashMap<>(data.characterScales);
        this.intendedMusic = data.currentMusic;
        this.customDialogPosition = data.dialogPosition;
        this.currentWindowTitle = data.windowTitle != null ? data.windowTitle : "VisualBean";

        SwingUtilities.invokeLater(() -> {
            if (data.windowSize != null) {
                window.setSize(data.windowSize);
            }
            if (data.windowPosition != null) {
                window.setLocation(data.windowPosition);
            } else {
                window.setLocationRelativeTo(null);
            }
            window.setTitle(currentWindowTitle);
            window.repaint();
        });

        if (intendedMusic != null) {
            audioManager.playMusic(intendedMusic, true);
        } else {
            audioManager.stopMusic();
        }

        this.waitingForClick = false;
        this.isMainMenu = false;

        // Replay script in skip-mode to rebuild logical state (variables, etc.)
        if (currentScript != null) {
            if (scriptThread != null && scriptThread.isAlive()) {
                scriptCancelled = true;
                synchronized (this) {
                    notifyAll();
                }
                scriptThread.interrupt();
                try {
                    scriptThread.join(500);
                } catch (InterruptedException e) {
                }
            }
            executeScript(currentScript);
        } else if (startGameCallback != null) {
            startGameCallback.run();
        }
    }

    public boolean isSkipping() {
        return targetStep != -1 && currentStep < targetStep;
    }

    public void say(String name, String text) {
        if (isSkipping()) {
            // Skip mode: only increment step counter and rebuild history
            this.currentStep++;
            this.backlog.add(new LogEntry(name, text));
            return;
        }

        // Reached target step, exit skip mode
        if (targetStep != -1 && currentStep == targetStep) {
            targetStep = -1;
            window.updateDialogue(name, text);
        }

        this.currentStep++;

        this.currentSpeaker = name;
        this.currentDialogue = text;
        this.backlog.add(new LogEntry(name, text));
        this.waitingForClick = true;

        window.updateDialogue(name, text);
        waitForInput();
    }

    private void waitForInput() {
        synchronized (this) {
            while (waitingForClick && !scriptCancelled) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    // Interrupted (e.g. by loadGame), stop waiting
                    waitingForClick = false;
                }
            }

            if (scriptCancelled) {
                throw new RuntimeException("Script cancelled");
            }
        }
    }

    public void onUserClick() {
        // If text is still animating, complete it first before advancing
        if (window.isTextAnimating()) {
            window.skipTextAnimation();
            return;
        }

        synchronized (this) {
            if (waitingForClick) {
                waitingForClick = false;
                notifyAll();
            }
        }
    }

    public java.util.List<LogEntry> getBacklog() {
        return backlog;
    }

    public boolean isAutoMode() {
        return autoMode;
    }

    public void setAutoMode(boolean autoMode) {
        this.autoMode = autoMode;
    }

    public void executeScript(Runnable script) {
        this.currentScript = script;
        this.scriptCancelled = false;
        this.scriptThread = new Thread(() -> {
            try {
                script.run();
                targetStep = -1;
            } catch (Exception e) {
                if (!(e instanceof RuntimeException && e.getMessage().equals("Script cancelled"))) {
                }
            }
        });
        this.scriptThread.start();
    }

    // Choice system
    private String[] currentOptions;
    private int selectedOptionIndex = -1;

    public int promptChoice(String[] options) {
        if (isSkipping()) {
            targetStep = -1;
        }

        this.currentStep++;

        synchronized (this) {
            this.currentOptions = options;
            this.selectedOptionIndex = -1;
            this.waitingForClick = false;
            window.repaint();

            while (selectedOptionIndex == -1 && !scriptCancelled) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    if (scriptCancelled) {
                        throw new RuntimeException("Script cancelled");
                    }
                    return 0;
                }
            }
            if (scriptCancelled) {
                throw new RuntimeException("Script cancelled");
            }
            this.currentOptions = null;
            window.repaint();
            return selectedOptionIndex;
        }
    }

    public void onOptionSelected(int index) {
        synchronized (this) {
            this.selectedOptionIndex = index;
            notifyAll();
        }
    }

    // Window manipulation (meta features)

    public void setWindowPosition(int x, int y) {
        if (isSkipping())
            return;
        SwingUtilities.invokeLater(() -> window.setLocation(x, y));
    }

    public Point getWindowPosition() {
        return window.getLocation();
    }

    public void centerWindow() {
        if (isSkipping())
            return;
        centerWindow(0, null);
    }

    public void centerWindow(int durationMs, com.visualbean.util.Easing easing) {
        if (isSkipping())
            return;

        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = window.getSize();
        int targetX = (screenSize.width - windowSize.width) / 2;
        int targetY = (screenSize.height - windowSize.height) / 2;

        if (durationMs <= 0 || easing == null) {
            SwingUtilities.invokeLater(() -> window.setLocation(targetX, targetY));
        } else {
            slideWindow(targetX, targetY, durationMs, easing);
        }
    }

    public void shakeWindow(int intensity, int durationMs) {
        if (isSkipping())
            return;

        if (windowAnimation != null && windowAnimation.isAlive()) {
            windowAnimation.interrupt();
        }

        windowAnimation = new Thread(() -> {
            Point original = window.getLocation();
            long endTime = System.currentTimeMillis() + durationMs;

            while (System.currentTimeMillis() < endTime && !Thread.currentThread().isInterrupted()) {
                int offsetX = (int) (Math.random() * intensity * 2) - intensity;
                int offsetY = (int) (Math.random() * intensity * 2) - intensity;

                SwingUtilities.invokeLater(() -> window.setLocation(original.x + offsetX, original.y + offsetY));

                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    SwingUtilities.invokeLater(() -> window.setLocation(original));
                    return;
                }
            }
            if (!Thread.currentThread().isInterrupted()) {
                SwingUtilities.invokeLater(() -> window.setLocation(original));
            }
        });
        windowAnimation.start();
    }

    public void slideWindow(int targetX, int targetY, int durationMs) {
        slideWindow(targetX, targetY, durationMs, com.visualbean.util.Easing.EASE_IN_OUT_CUBIC);
    }

    public void slideWindow(int targetX, int targetY, int durationMs, com.visualbean.util.Easing easing) {
        if (isSkipping()) {
            setWindowPosition(targetX, targetY);
            return;
        }

        if (windowAnimation != null && windowAnimation.isAlive()) {
            windowAnimation.interrupt();
        }

        windowAnimation = new Thread(() -> {
            Point start = window.getLocation();
            long startTime = System.currentTimeMillis();

            while (!Thread.currentThread().isInterrupted()) {
                long now = System.currentTimeMillis();
                float progress = (float) (now - startTime) / durationMs;
                if (progress >= 1f)
                    break;

                float easedProgress = easing.apply(progress);

                int currentX = (int) (start.x + (targetX - start.x) * easedProgress);
                int currentY = (int) (start.y + (targetY - start.y) * easedProgress);

                SwingUtilities.invokeLater(() -> window.setLocation(currentX, currentY));

                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    return;
                }
            }
            if (!Thread.currentThread().isInterrupted()) {
                SwingUtilities.invokeLater(() -> window.setLocation(targetX, targetY));
            }
        });
        windowAnimation.start();
    }

    // Character animation

    public void slideCharacter(String name, int targetX, int targetY, int durationMs,
            com.visualbean.util.Easing easing) {
        if (!visibleCharacters.containsKey(name))
            return;

        if (isSkipping()) {
            characterPositions.put(name, new Point(targetX, targetY));

            return;
        }

        Thread existingAnim = characterAnimations.get(name);
        if (existingAnim != null && existingAnim.isAlive()) {
            existingAnim.interrupt();
        }

        Thread animThread = new Thread(() -> {
            Point start = characterPositions.getOrDefault(name, new Point(0, 0));
            if (!characterPositions.containsKey(name)) {
                characterPositions.put(name, new Point(targetX, targetY));
                window.repaint();
                return;
            }

            long startTime = System.currentTimeMillis();

            while (!Thread.currentThread().isInterrupted()) {
                long now = System.currentTimeMillis();
                float progress = (float) (now - startTime) / durationMs;
                if (progress >= 1f)
                    break;

                float easedProgress = easing.apply(progress);

                int currentX = (int) (start.x + (targetX - start.x) * easedProgress);
                int currentY = (int) (start.y + (targetY - start.y) * easedProgress);

                characterPositions.put(name, new Point(currentX, currentY));
                SwingUtilities.invokeLater(() -> window.repaint());

                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    return;
                }
            }
            if (!Thread.currentThread().isInterrupted()) {
                characterPositions.put(name, new Point(targetX, targetY));
                SwingUtilities.invokeLater(() -> window.repaint());
            }
        });
        characterAnimations.put(name, animThread);
        animThread.start();
    }

    public void slideDialog(int targetX, int targetY, int durationMs, com.visualbean.util.Easing easing) {
        if (isSkipping()) {
            setDialogPosition(targetX, targetY);
            return;
        }

        if (dialogAnimation != null && dialogAnimation.isAlive()) {
            dialogAnimation.interrupt();
        }

        dialogAnimation = new Thread(() -> {
            Point start = customDialogPosition != null ? customDialogPosition : new Point(20, 500);
            long startTime = System.currentTimeMillis();

            while (!Thread.currentThread().isInterrupted()) {
                long now = System.currentTimeMillis();
                float progress = (float) (now - startTime) / durationMs;
                if (progress >= 1f)
                    break;

                float easedProgress = easing.apply(progress);

                int currentX = (int) (start.x + (targetX - start.x) * easedProgress);
                int currentY = (int) (start.y + (targetY - start.y) * easedProgress);

                setDialogPosition(currentX, currentY);

                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    return;
                }
            }
            if (!Thread.currentThread().isInterrupted()) {
                setDialogPosition(targetX, targetY);
            }
        });
        dialogAnimation.start();
    }

    public void resizeWindow(int targetW, int targetH, int durationMs) {
        resizeWindow(targetW, targetH, durationMs, com.visualbean.util.Easing.EASE_IN_OUT_CUBIC, false);
    }

    public void resizeWindow(int targetW, int targetH, int durationMs, com.visualbean.util.Easing easing) {
        resizeWindow(targetW, targetH, durationMs, easing, false);
    }

    public void resizeWindow(int targetW, int targetH, int durationMs, com.visualbean.util.Easing easing,
            boolean keepCentered) {
        if (isSkipping()) {
            setWindowSize(targetW, targetH);
            if (keepCentered)
                centerWindow();
            return;
        }

        if (windowResizeAnimation != null && windowResizeAnimation.isAlive()) {
            windowResizeAnimation.interrupt();
        }

        windowResizeAnimation = new Thread(() -> {
            Dimension startSize = window.getSize();
            long startTime = System.currentTimeMillis();

            Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();

            while (!Thread.currentThread().isInterrupted()) {
                long now = System.currentTimeMillis();
                float progress = (float) (now - startTime) / durationMs;
                if (progress >= 1f)
                    break;

                float easedProgress = easing.apply(progress);

                int currentW = (int) (startSize.width + (targetW - startSize.width) * easedProgress);
                int currentH = (int) (startSize.height + (targetH - startSize.height) * easedProgress);

                SwingUtilities.invokeLater(() -> {
                    window.setSize(currentW, currentH);
                    if (keepCentered) {
                        int x = (screenSize.width - currentW) / 2;
                        int y = (screenSize.height - currentH) / 2;
                        window.setLocation(x, y);
                    }
                    window.revalidate();
                });

                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    return;
                }
            }
            if (!Thread.currentThread().isInterrupted()) {
                SwingUtilities.invokeLater(() -> {
                    window.setSize(targetW, targetH);
                    if (keepCentered) {
                        window.setLocationRelativeTo(null);
                    }
                });
            }
        });
        windowResizeAnimation.start();
    }

    // Website opener
    public void openWebsite(String urlString) {
        if (isSkipping())
            return;

        try {
            java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
            if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                java.net.URI uri = new java.net.URI(urlString);
                desktop.browse(uri);
            } else {
                System.err.println("Web browsing not supported on this platform.");
            }
        } catch (Exception e) {
            System.err.println("Error opening website: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // System tray notification (cross-platform)
    private java.awt.TrayIcon trayIcon;

    public void showNotification(String title, String message) {
        if (isSkipping())
            return;

        if (!java.awt.SystemTray.isSupported()) {
            System.err.println("System tray notifications are not supported on this platform.");
            return;
        }

        try {
            java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();

            // Create tray icon only once, reuse for subsequent notifications
            if (trayIcon == null) {
                // Create a small icon for the tray
                java.awt.Image icon = new java.awt.image.BufferedImage(16, 16,
                        java.awt.image.BufferedImage.TYPE_INT_ARGB);
                java.awt.Graphics2D g = (java.awt.Graphics2D) icon.getGraphics();
                g.setColor(new java.awt.Color(100, 149, 237));
                g.fillRoundRect(0, 0, 16, 16, 4, 4);
                g.setColor(java.awt.Color.WHITE);
                g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 11));
                g.drawString("V", 3, 13);
                g.dispose();

                trayIcon = new java.awt.TrayIcon(icon, "VisualBean");
                trayIcon.setImageAutoSize(true);
                tray.add(trayIcon);
            }

            trayIcon.displayMessage(title, message, java.awt.TrayIcon.MessageType.INFO);

            // Auto-remove tray icon after a delay so it doesn't clutter
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    if (trayIcon != null) {
                        java.awt.SystemTray.getSystemTray().remove(trayIcon);
                        trayIcon = null;
                    }
                } catch (Exception ignored) {
                }
            }).start();
        } catch (Exception e) {
            System.err.println("Error showing notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Desktop wallpaper (meta feature)
    public String getDesktopWallpaper() {
        return WallpaperManager.getCurrentWallpaper();
    }

    public void setDesktopWallpaper(String imagePath) {
        if (isSkipping())
            return;

        WallpaperManager.saveCurrentWallpaper();
        WallpaperManager.setWallpaper(imagePath);
    }

    public void restoreDesktopWallpaper() {
        if (isSkipping())
            return;

        WallpaperManager.restoreWallpaper();
    }

    public void scaleCharacter(String name, double targetScale, int durationMs, com.visualbean.util.Easing easing) {
        if (!visibleCharacters.containsKey(name))
            return;

        if (isSkipping()) {
            characterScales.put(name, targetScale);
            return;
        }

        Thread existingAnim = characterScaleAnimations.get(name);
        if (existingAnim != null && existingAnim.isAlive()) {
            existingAnim.interrupt();
        }

        Thread animThread = new Thread(() -> {
            double startScale = characterScales.getOrDefault(name, 1.0);
            long startTime = System.currentTimeMillis();

            while (!Thread.currentThread().isInterrupted()) {
                long now = System.currentTimeMillis();
                float progress = (float) (now - startTime) / durationMs;
                if (progress >= 1f)
                    break;

                float easedProgress = easing.apply(progress);

                double currentScale = startScale + (targetScale - startScale) * easedProgress;

                characterScales.put(name, currentScale);
                SwingUtilities.invokeLater(() -> window.repaint());

                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    return;
                }
            }
            if (!Thread.currentThread().isInterrupted()) {
                characterScales.put(name, targetScale);
                SwingUtilities.invokeLater(() -> window.repaint());
            }
        });
        characterScaleAnimations.put(name, animThread);
        animThread.start();
    }

    // Main menu system
    private boolean isMainMenu = false;
    private Runnable startGameCallback;

    public void showMainMenu(Runnable onStart) {
        this.startGameCallback = onStart;
        this.isMainMenu = true;

        this.currentBackground = null;
        this.visibleCharacters.clear();
        this.characterPositions.clear();
        this.characterScales.clear();
        this.currentDialogue = null;
        this.currentSpeaker = null;

        playMusic("Enjoy", true);

        window.repaint();
    }

    public boolean isMainMenu() {
        return isMainMenu;
    }

    public void startGame() {
        if (startGameCallback != null) {
            this.isMainMenu = false;
            stopMusic();
            startGameCallback.run();
        }
    }

    // Sub-window system (meta feature)

    public void createSubWindow(String id, String title, int width, int height) {
        if (isSkipping())
            return;
        SwingUtilities.invokeLater(() -> {
            if (subWindows.containsKey(id)) {
                subWindows.get(id).dispose();
            }
            SubWindow sw = new SubWindow(id, title, width, height);
            subWindows.put(id, sw);
        });
    }

    public void closeSubWindow(String id) {
        if (isSkipping())
            return;
        SwingUtilities.invokeLater(() -> {
            SubWindow sw = subWindows.remove(id);
            if (sw != null) {
                sw.dispose();
            }
        });
    }

    public void setSubWindowPosition(String id, int x, int y) {
        if (isSkipping())
            return;
        SwingUtilities.invokeLater(() -> {
            SubWindow sw = subWindows.get(id);
            if (sw != null) {
                sw.setLocation(x, y);
            }
        });
    }

    public void setSubWindowBackground(String id, String imagePath) {
        if (isSkipping())
            return;
        SwingUtilities.invokeLater(() -> {
            SubWindow sw = subWindows.get(id);
            if (sw != null) {
                sw.setBackground(imagePath);
            }
        });
    }

    public void showCharacterInSubWindow(String id, String name, String imagePath, int x, int y) {
        showCharacterInSubWindow(id, name, imagePath, x, y, 1.0);
    }

    public void showCharacterInSubWindow(String id, String name, String imagePath, int x, int y, double scale) {
        if (isSkipping())
            return;
        SwingUtilities.invokeLater(() -> {
            SubWindow sw = subWindows.get(id);
            if (sw != null) {
                sw.showCharacter(name, imagePath, x, y, scale);
            }
        });
    }

    public void moveCharacterInSubWindow(String id, String name, int x, int y) {
        if (isSkipping())
            return;
        SwingUtilities.invokeLater(() -> {
            SubWindow sw = subWindows.get(id);
            if (sw != null) {
                sw.moveCharacter(name, x, y);
            }
        });
    }

    public void subWindowSay(String id, String name, String text) {
        if (isSkipping())
            return;
        SwingUtilities.invokeLater(() -> {
            SubWindow sw = subWindows.get(id);
            if (sw != null) {
                sw.setText(name, text);
            }
        });
    }
}
