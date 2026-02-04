package com.vnengine.ui;

import com.vnengine.core.GameEngine;
import com.vnengine.core.SaveManager;
import com.vnengine.core.SaveData;
import com.vnengine.core.SettingsManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import com.vnengine.core.GameEngine.LogEntry;

public class GameWindow extends JFrame {
    private GameEngine engine;
    private GamePanel panel;
    private boolean uiVisible = true;

    // Overlay panel states
    private enum OverlayState {
        NONE, HISTORY, SAVE, LOAD, SETTINGS
    }

    private OverlayState currentOverlay = OverlayState.NONE;

    // Scaling constants
    private final int BASE_WIDTH = 1280;
    private final int BASE_HEIGHT = 720;

    private double getScaleFactor() {
        if (panel == null || panel.getWidth() == 0)
            return 1.0;
        return Math.min((double) panel.getWidth() / BASE_WIDTH, (double) panel.getHeight() / BASE_HEIGHT);
    }

    private int scale(int value) {
        return (int) (value * getScaleFactor());
    }

    private Font scale(Font font) {
        return font.deriveFont(font.getSize() * (float) getScaleFactor());
    }

    // For history scrolling
    private int historyScrollOffset = 0;
    private int maxHistoryScroll = 0;

    // For save/load slot hover
    private int hoveredSlot = -1;

    // Animation for overlay fade
    private float overlayAlpha = 0f;
    private OverlayState targetOverlay = OverlayState.NONE;

    public void applySettings() {
        SettingsManager sm = SettingsManager.getInstance();
        panel.renderer.setTypeSpeed(sm.getTextSpeed());

        // Check fullscreen sync
        boolean isFs = isUndecorated() && getExtendedState() == JFrame.MAXIMIZED_BOTH; // approx
        // Actually setFullscreen handles it.
        // We can just call it to be sure if different?
        // But be careful of recreating window.
        // Let's just trust initial set.
    }

    public void setFullscreen(boolean fullscreen) {
        if (fullscreen == isUndecorated())
            return; // Already in desired state (approximated check)

        dispose(); // Must dispose before changing decoration style

        if (fullscreen) {
            setUndecorated(true);
            setResizable(false);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            if (gd.isFullScreenSupported()) {
                gd.setFullScreenWindow(this);
            } else {
                setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
            setVisible(true);
        } else {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            gd.setFullScreenWindow(null); // Exit full screen exclusive

            setUndecorated(false);
            setResizable(false);
            setExtendedState(JFrame.NORMAL);

            panel.setPreferredSize(new Dimension(1280, 720));
            // First pack to set initial bounds using L&F insets
            pack();
            setLocationRelativeTo(null);

            setVisible(true);

            // Second pack to correct for native peer insets (Thick borders)
            // This fixes the issue on subsequent toggles where insets might need
            // recalculation
            SwingUtilities.invokeLater(() -> {
                pack();
                setLocationRelativeTo(null);
                panel.repaint();
            });
        }
    }

    public GameWindow(GameEngine engine) {
        this.engine = engine;
        setTitle("VN Engine - Demo Showcase");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new GamePanel();
        panel.setPreferredSize(new Dimension(1280, 720));
        panel.setFocusable(true);
        panel.requestFocusInWindow();

        add(panel);

        setResizable(false); // Set resizable BEFORE pack to ensure correct border calculation
        pack(); // Size window to fit panel including decorations
        setLocationRelativeTo(null);

        // Prevent maximization in windowed mode (effectively disabling the button
        // function)
        addWindowStateListener(e -> {
            if ((e.getNewState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
                // Only prevent if we are NOT in our custom fullscreen mode (which might use
                // MAXIMIZED_BOTH)
                if (!isUndecorated()) {
                    setExtendedState(JFrame.NORMAL);
                }
            }
        });

        // Key Bindings for SPACE and ENTER
        InputMap im = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = panel.getActionMap();

        im.put(KeyStroke.getKeyStroke("SPACE"), "advance");
        im.put(KeyStroke.getKeyStroke("ENTER"), "advance");
        im.put(KeyStroke.getKeyStroke("ESCAPE"), "closeOverlay");

        am.put("advance", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (currentOverlay != OverlayState.NONE) {
                    closeOverlay();
                    return;
                }
                String[] options = engine.getCurrentOptions();
                if (options == null) {
                    engine.onUserClick();
                }
            }
        });

        am.put("closeOverlay", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (currentOverlay != OverlayState.NONE) {
                    closeOverlay();
                }
            }
        });

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                panel.requestFocusInWindow();

                // Fake error handled by Swing dialog now

                // If overlay is open, handle overlay clicks
                if (currentOverlay != OverlayState.NONE) {
                    handleOverlayClick(e);
                    return;
                }

                if (engine.isMainMenu()) {
                    handleMainMenuClick(e);
                    return;
                }

                // Right click toggles UI
                if (SwingUtilities.isRightMouseButton(e)) {
                    uiVisible = !uiVisible;
                    panel.repaint();
                    return;
                }

                if (!uiVisible) {
                    uiVisible = true;
                    panel.repaint();
                    return;
                }

                int tbW = scale(80);
                int tbH = scale(30);
                int tbY = scale(10);
                int histX = panel.getWidth() - scale(100);
                int saveX = histX - tbW - scale(10);
                int loadX = saveX - tbW - scale(10);

                // Toolbar checks
                if (e.getY() >= tbY && e.getY() <= tbY + tbH) {
                    if (e.getX() >= histX && e.getX() <= histX + tbW) {
                        openOverlay(OverlayState.HISTORY);
                        return;
                    }
                    if (e.getX() >= saveX && e.getX() <= saveX + tbW) {
                        openOverlay(OverlayState.SAVE);
                        return;
                    }
                    if (e.getX() >= loadX && e.getX() <= loadX + tbW) {
                        openOverlay(OverlayState.LOAD);
                        return;
                    }
                    if (e.getX() >= loadX - tbW - scale(10) && e.getX() <= loadX - scale(10)) {
                        openOverlay(OverlayState.SETTINGS);
                        return;
                    }
                }

                // Check for options
                String[] options = engine.getCurrentOptions();
                if (options != null) {
                    int idx = panel.getOptionAt(e.getX(), e.getY());
                    if (idx != -1) {
                        engine.onOptionSelected(idx);
                    }
                } else {
                    engine.onUserClick();
                }
            }
        });

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (currentOverlay == OverlayState.SAVE || currentOverlay == OverlayState.LOAD) {
                    int newHovered = getSlotAtPosition(e.getX(), e.getY());
                    if (newHovered != hoveredSlot) {
                        hoveredSlot = newHovered;
                        panel.repaint();
                    }
                }
            }
        });

        panel.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (currentOverlay == OverlayState.HISTORY) {
                    historyScrollOffset += e.getWheelRotation() * 40;
                    historyScrollOffset = Math.max(0, Math.min(historyScrollOffset, maxHistoryScroll));
                    panel.repaint();
                }
            }
        });

        add(panel);

        // Animation timer
        Timer timer = new Timer(16, e -> {
            panel.update();

            // Animate overlay alpha
            if (targetOverlay != OverlayState.NONE && overlayAlpha < 1f) {
                overlayAlpha = Math.min(1f, overlayAlpha + 0.1f);
            } else if (targetOverlay == OverlayState.NONE && overlayAlpha > 0f) {
                overlayAlpha = Math.max(0f, overlayAlpha - 0.1f);
                if (overlayAlpha == 0f) {
                    currentOverlay = OverlayState.NONE;
                }
            }

            panel.repaint();
        });
        timer.start();
    }

    private void openOverlay(OverlayState state) {
        if (state == OverlayState.HISTORY) {
            historyScrollOffset = 0;
        }
        hoveredSlot = -1;
        currentOverlay = state;
        targetOverlay = state;
    }

    private void closeOverlay() {
        targetOverlay = OverlayState.NONE;
        hoveredSlot = -1;
    }

    private int getSlotAtPosition(int mx, int my) {
        StyleManager sm = StyleManager.getInstance();
        int overlayWidth = scale(sm.getInt(".overlay-panel", "width", 700));
        int overlayHeight = scale(sm.getInt(".overlay-panel", "height", 500));
        int overlayX = (panel.getWidth() - overlayWidth) / 2;
        int overlayY = (panel.getHeight() - overlayHeight) / 2;

        int slotWidth = scale(sm.getInt(".save-slot", "width", 200));
        int slotHeight = scale(sm.getInt(".save-slot", "height", 120));
        int cols = 3;
        int startX = overlayX + scale(40);
        int startY = overlayY + scale(80);
        int gapX = scale(20);
        int gapY = scale(20);

        for (int i = 0; i < 9; i++) {
            int col = i % cols;
            int row = i / cols;
            int slotX = startX + col * (slotWidth + gapX);
            int slotY = startY + row * (slotHeight + gapY);

            if (mx >= slotX && mx <= slotX + slotWidth && my >= slotY && my <= slotY + slotHeight) {
                return i + 1;
            }
        }
        return -1;
    }

    private void handleMainMenuClick(MouseEvent e) {
        StyleManager sm = StyleManager.getInstance();
        int w = panel.getWidth();
        int h = panel.getHeight();

        int btnW = scale(sm.getInt(".main-menu-button", "width", 300));
        int btnH = scale(sm.getInt(".main-menu-button", "height", 60));
        int startY = h / 2;
        int gap = scale(sm.getInt(".main-menu-button", "gap", 30));

        int mx = e.getX();
        int my = e.getY();

        int btnX = (w - btnW) / 2;

        // New Game
        if (mx >= btnX && mx <= btnX + btnW && my >= startY && my <= startY + btnH) {
            engine.startGame();
            return;
        }

        // Load Game
        int loadY = startY + btnH + gap;
        if (mx >= btnX && mx <= btnX + btnW && my >= loadY && my <= loadY + btnH) {
            openOverlay(OverlayState.LOAD);
            return;
        }

        // Settings
        int settingsY = loadY + btnH + gap;
        if (mx >= btnX && mx <= btnX + btnW && my >= settingsY && my <= settingsY + btnH) {
            openOverlay(OverlayState.SETTINGS);
            return;
        }

        // Exit
        int exitY = settingsY + btnH + gap;
        if (mx >= btnX && mx <= btnX + btnW && my >= exitY && my <= exitY + btnH) {
            System.exit(0);
        }
    }

    private void handleOverlayClick(MouseEvent e) {
        StyleManager sm = StyleManager.getInstance();
        int overlayWidth = scale(sm.getInt(".overlay-panel", "width", 700));
        int overlayHeight = scale(sm.getInt(".overlay-panel", "height", 500));
        int overlayX = (panel.getWidth() - overlayWidth) / 2;
        int overlayY = (panel.getHeight() - overlayHeight) / 2;

        // Check close button (top right of panel)
        int closeW = scale(sm.getInt(".overlay-close", "width", 35));
        int closeH = scale(sm.getInt(".overlay-close", "height", 35));
        int closeX = overlayX + overlayWidth - closeW - scale(15);
        int closeY = overlayY + scale(15);
        if (e.getX() >= closeX && e.getX() <= closeX + closeW && e.getY() >= closeY && e.getY() <= closeY + closeH) {
            closeOverlay();
            return;
        }

        // Check if clicking outside overlay
        if (e.getX() < overlayX || e.getX() > overlayX + overlayWidth ||
                e.getY() < overlayY || e.getY() > overlayY + overlayHeight) {
            closeOverlay();
            return;
        }

        // Settings Interaction
        if (currentOverlay == OverlayState.SETTINGS) {
            int startY = overlayY + scale(100);
            int gapY = scale(70);
            int sliderWidth = scale(300);
            int sliderHeight = scale(20);
            int labelWidth = scale(150);
            int contentX = overlayX + (overlayWidth - (labelWidth + sliderWidth + scale(20))) / 2;

            // 1. Music Volume
            int rowY = startY;
            int sliderX = contentX + labelWidth + scale(20);
            if (e.getY() >= rowY - scale(10) && e.getY() <= rowY + sliderHeight + scale(10) &&
                    e.getX() >= sliderX && e.getX() <= sliderX + sliderWidth) {
                float val = (float) (e.getX() - sliderX) / sliderWidth;
                SettingsManager.getInstance().setMusicVolume(val);
                engine.applySettings();
                panel.repaint();
                return;
            }

            // 2. SFX Volume
            rowY += gapY;
            if (e.getY() >= rowY - scale(10) && e.getY() <= rowY + sliderHeight + scale(10) &&
                    e.getX() >= sliderX && e.getX() <= sliderX + sliderWidth) {
                float val = (float) (e.getX() - sliderX) / sliderWidth;
                SettingsManager.getInstance().setSfxVolume(val);
                engine.applySettings();
                panel.repaint();
                return;
            }

            // 3. Text Speed
            rowY += gapY;
            if (e.getY() >= rowY - scale(10) && e.getY() <= rowY + sliderHeight + scale(10) &&
                    e.getX() >= sliderX && e.getX() <= sliderX + sliderWidth) {
                float val = (float) (e.getX() - sliderX) / sliderWidth;
                // Speed range: 0.1 (slow) to 3.0 (fast)
                float speed = 0.1f + val * 2.9f;
                SettingsManager.getInstance().setTextSpeed(speed);
                engine.applySettings();
                panel.repaint();
                return;
            }

            // 4. Fullscreen
            rowY += gapY;
            if (e.getY() >= rowY && e.getY() <= rowY + scale(30) &&
                    e.getX() >= sliderX && e.getX() <= sliderX + scale(30)) {
                boolean fs = SettingsManager.getInstance().isFullscreen();
                SettingsManager.getInstance().setFullscreen(!fs);
                setFullscreen(!fs);
                panel.repaint();
                return;
            }
            return;
        }

        // Save/Load slot click
        if (currentOverlay == OverlayState.SAVE || currentOverlay == OverlayState.LOAD) {

            // Check delete button clicks
            int slotWidth = scale(sm.getInt(".save-slot", "width", 200));
            int slotHeight = scale(sm.getInt(".save-slot", "height", 120));
            int cols = 3;
            int startX = overlayX + scale(40);
            int startY = overlayY + scale(80);
            int gapX = scale(20);
            int gapY = scale(20);

            for (int i = 0; i < 9; i++) {
                int col = i % cols;
                int row = i / cols;
                int slotX = startX + col * (slotWidth + gapX);
                int slotY = startY + row * (slotHeight + gapY);

                SaveData data = SaveManager.load(i + 1);
                if (data != null) {
                    int delSize = scale(20);
                    int delX = slotX + slotWidth - delSize - scale(5);
                    int delY = slotY + scale(35);

                    if (e.getX() >= delX && e.getX() <= delX + delSize &&
                            e.getY() >= delY && e.getY() <= delY + delSize) {
                        SaveManager.delete(i + 1);
                        panel.repaint();
                        return;
                    }
                }
            }

            int slot = getSlotAtPosition(e.getX(), e.getY());
            if (slot != -1) {
                try {
                    if (currentOverlay == OverlayState.SAVE) {
                        engine.saveGame(slot);
                        closeOverlay();
                    } else {
                        engine.loadGame(slot);
                        closeOverlay();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public boolean isTextAnimating() {
        return !panel.renderer.isFinished();
    }

    public void skipTextAnimation() {
        panel.renderer.skip();
    }

    public void updateDialogue(String name, String text) {
        panel.renderer.setText(text);
        panel.repaint();
    }

    private Map<String, BufferedImage> imageCache = new HashMap<>();

    private BufferedImage loadImage(String name) {
        if (imageCache.containsKey(name)) {
            return imageCache.get(name);
        }
        try {
            String[] extensions = { ".png", ".jpg", ".jpeg" };
            String[] searchPaths = {
                    "resources/images/",
                    "resources/backgrounds/",
                    "resources/characters/",
                    "resources/"
            };

            File file = null;

            for (String path : searchPaths) {
                for (String ext : extensions) {
                    File f = new File(path + name + ext);
                    if (f.exists()) {
                        file = f;
                        break;
                    }
                }
                if (file != null)
                    break;

                File f = new File(path + name);
                if (f.exists()) {
                    file = f;
                    break;
                }
            }

            if (file != null) {
                BufferedImage img = ImageIO.read(file);
                imageCache.put(name, img);
                return img;
            } else {
                System.err.println("Image not found: " + name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private class GamePanel extends JPanel {
        KineticTextRenderer renderer = new KineticTextRenderer();

        public void update() {
            renderer.update();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (engine.isMainMenu()) {
                drawMainMenu(g2d);
                if (currentOverlay != OverlayState.NONE || overlayAlpha > 0) {
                    drawOverlay(g2d);
                }
                return;
            }

            // 1. Draw Background
            String bgPath = engine.getCurrentBackground();
            if (bgPath != null) {
                if (bgPath.startsWith("#")) {
                    g2d.setColor(Color.decode(bgPath));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    BufferedImage bg = loadImage(bgPath);
                    if (bg != null) {
                        // Calculate scale to cover the screen while maintaining aspect ratio
                        double panelRatio = (double) getWidth() / getHeight();
                        double imgRatio = (double) bg.getWidth() / bg.getHeight();

                        int drawW, drawH, drawX, drawY;

                        if (panelRatio > imgRatio) {
                            // Panel is relatively wider: fit width, crop height
                            drawW = getWidth();
                            drawH = (int) (drawW / imgRatio);
                            drawX = 0;
                            drawY = (getHeight() - drawH) / 2;
                        } else {
                            // Panel is relatively taller: fit height, crop width
                            drawH = getHeight();
                            drawW = (int) (drawH * imgRatio);
                            drawX = (getWidth() - drawW) / 2;
                            drawY = 0;
                        }

                        g2d.drawImage(bg, drawX, drawY, drawW, drawH, null);
                    } else {
                        g2d.setColor(Color.DARK_GRAY);
                        g2d.fillRect(0, 0, getWidth(), getHeight());
                        g2d.setColor(Color.WHITE);
                        g2d.drawString("Missing BG: " + bgPath, 50, 50);
                    }
                }
            } else {
                g2d.setColor(Color.BLACK);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }

            // 2. Draw Characters
            Map<String, String> characters = engine.getVisibleCharacters();

            int totalWidth = characters.size() * scale(300);
            int charStartX = (getWidth() - totalWidth) / 2 + scale(50);
            int xOffset = charStartX;

            for (Map.Entry<String, String> entry : characters.entrySet()) {
                String name = entry.getKey();
                String spriteName = entry.getValue();
                BufferedImage sprite = loadImage(spriteName);

                int drawX = xOffset;
                int drawY = scale(150);

                Point customPos = engine.getCharacterPosition(name);
                if (customPos != null) {
                    drawX = scale(customPos.x); // Assuming engine stores unscaled coords
                    drawY = scale(customPos.y);
                } else {
                    xOffset += scale(300);
                }

                if (sprite != null) {
                    double charScale = engine.getCharacterScale(name);
                    int h = scale((int) (500 * charScale));
                    int w = (int) ((double) sprite.getWidth() / sprite.getHeight() * h);
                    g2d.drawImage(sprite, drawX, drawY, w, h, null);
                } else {
                    g2d.setColor(Color.BLUE);
                    g2d.fillRect(drawX, drawY, scale(200), scale(400));
                    g2d.setColor(Color.WHITE);
                    g2d.drawString(name, drawX + scale(50), drawY + scale(50));
                }
            }

            // Draw Dialogue Box Content
            if (uiVisible && currentOverlay == OverlayState.NONE) {
                drawDialogueBox(g2d);

                String speaker = engine.getCurrentSpeaker();
                String text = engine.getCurrentDialogue();
                String[] options = engine.getCurrentOptions();
                StyleManager sm = StyleManager.getInstance();

                int boxHeight = scale(200);
                int boxY = getHeight() - boxHeight - scale(20);
                int boxX = scale(20);
                Point dialogPos = engine.getCustomDialogPosition();
                if (dialogPos != null) {
                    boxX = scale(dialogPos.x);
                    boxY = scale(dialogPos.y);
                }

                // Get padding from CSS
                int paddingLeft = scale(sm.getInt(".dialog-box", "padding-left", 20));
                int paddingTop = scale(sm.getInt(".dialog-box", "padding-top", 20));
                int nameOffsetY = scale(sm.getInt(".dialog-box", "name-offset-y", 25));
                int textOffsetY = scale(sm.getInt(".dialog-box", "text-offset-y", 55));

                int textX = boxX + paddingLeft;
                int nameY = boxY + paddingTop + nameOffsetY;
                int textY = boxY + paddingTop + textOffsetY;

                if (options != null) {
                    g2d.setColor(new Color(0, 0, 0, 150));
                    g2d.fillRect(0, 0, getWidth(), getHeight());

                    int btnWidth = scale(sm.getInt(".menu-button", "width", 600));
                    int btnHeight = scale(sm.getInt(".menu-button", "height", 60));
                    int btnRadius = scale(sm.getInt(".menu-button", "border-radius", 10));
                    int startY = (getHeight() - (options.length * (btnHeight + scale(20)))) / 2;
                    int optStartX = (getWidth() - btnWidth) / 2;

                    Font btnFont = scale(sm.getFont(".menu-button", Font.BOLD, 24));
                    g2d.setFont(btnFont);

                    Color btnBg = sm.getColor(".menu-button", "background-color", new Color(50, 50, 50));
                    Color btnText = sm.getColor(".menu-button", "text-color", Color.WHITE);
                    Color btnBorder = sm.getColor(".menu-button", "border-color", Color.WHITE);

                    for (int i = 0; i < options.length; i++) {
                        int y = startY + i * (btnHeight + scale(20));

                        g2d.setColor(new Color(btnBg.getRed(), btnBg.getGreen(), btnBg.getBlue(), 220));
                        g2d.fillRoundRect(optStartX, y, btnWidth, btnHeight, btnRadius, btnRadius);

                        g2d.setColor(btnBorder);
                        g2d.drawRoundRect(optStartX, y, btnWidth, btnHeight, btnRadius, btnRadius);

                        g2d.setColor(btnText);
                        FontMetrics fm = g2d.getFontMetrics();
                        int textWidth = fm.stringWidth(options[i]);
                        int textHeight = fm.getAscent();
                        g2d.drawString(options[i], optStartX + (btnWidth - textWidth) / 2,
                                y + (btnHeight + textHeight) / 2 - 2);
                    }
                } else if (text != null) {
                    // Draw Toolbar
                    int tbW = scale(sm.getInt(".toolbar-button", "width", 80));
                    int tbH = scale(sm.getInt(".toolbar-button", "height", 30));
                    int tbY = scale(10);

                    int histX = getWidth() - tbW - scale(20);
                    drawToolbarButton(g2d, "History", histX, tbY, tbW, tbH);

                    int saveX = histX - tbW - scale(10);
                    drawToolbarButton(g2d, "Save", saveX, tbY, tbW, tbH);

                    int loadX = saveX - tbW - scale(10);
                    drawToolbarButton(g2d, "Load", loadX, tbY, tbW, tbH);

                    int settingsX = loadX - tbW - scale(10);
                    drawToolbarButton(g2d, "Config", settingsX, tbY, tbW, tbH);

                    // Draw speaker name
                    if (speaker != null) {
                        int nameFontSize = sm.getInt(".dialog-box", "name-font-size", 28);
                        g2d.setFont(scale(new Font(sm.getString(".dialog-box", "font-family", "SansSerif"), Font.BOLD,
                                nameFontSize)));
                        Color nameColor = sm.getColor(".dialog-box", "name-color", new Color(255, 200, 100));
                        g2d.setColor(nameColor);
                        g2d.drawString(speaker, textX, nameY);
                    }

                    // Draw dialogue text using renderer
                    int maxWidth = getWidth() - boxX - paddingLeft
                            - scale(sm.getInt(".dialog-box", "padding-right", 40));

                    // Update renderer font
                    renderer.setFont(scale(new Font("SansSerif", Font.PLAIN, 24)));
                    renderer.draw(g2d, textX, textY, maxWidth);
                }
            }

            // Draw overlay panels
            if (currentOverlay != OverlayState.NONE || overlayAlpha > 0) {
                drawOverlay(g2d);
            }

        }

        private void drawMainMenu(Graphics2D g2d) {
            StyleManager sm = StyleManager.getInstance();
            int w = getWidth();
            int h = getHeight();

            // Background
            Color bgTop = sm.getColor(".main-menu", "background-color-top", new Color(20, 20, 35));
            Color bgBottom = sm.getColor(".main-menu", "background-color-bottom", new Color(5, 5, 10));
            g2d.setPaint(new GradientPaint(0, 0, bgTop, 0, h, bgBottom));
            g2d.fillRect(0, 0, w, h);

            // Title
            String title = "Java Visual Novel Engine";

            Font titleFont = scale(sm.getFont(".main-menu-title", Font.BOLD, 60));
            g2d.setFont(titleFont);
            Color titleColor = sm.getColor(".main-menu-title", "text-color", new Color(220, 220, 255));
            g2d.setColor(titleColor);

            FontMetrics fm = g2d.getFontMetrics();
            int titleW = fm.stringWidth(title);

            float titleYRatio = sm.getFloat(".main-menu-title", "y-position-ratio", 0.33f);
            int titleY = (int) (h * titleYRatio);
            g2d.drawString(title, (w - titleW) / 2, titleY);

            // Buttons
            String[] buttons = { "New Game", "Load Game", "Settings", "Exit" };
            int btnW = scale(sm.getInt(".main-menu-button", "width", 300));
            int btnH = scale(sm.getInt(".main-menu-button", "height", 60));
            int gap = scale(sm.getInt(".main-menu-button", "gap", 30));
            int radius = scale(sm.getInt(".main-menu-button", "border-radius", 15));

            Color btnBg = sm.getColor(".main-menu-button", "background-color", new Color(50, 50, 70));
            Color btnBorder = sm.getColor(".main-menu-button", "border-color", new Color(100, 100, 150));
            Color btnText = sm.getColor(".main-menu-button", "text-color", Color.WHITE);
            Font btnFont = scale(sm.getFont(".main-menu-button", Font.PLAIN, 24));

            int startY = h / 2;

            for (int i = 0; i < buttons.length; i++) {
                int btnX = (w - btnW) / 2;
                int btnY = startY + i * (btnH + gap);

                g2d.setColor(btnBg);
                g2d.fillRoundRect(btnX, btnY, btnW, btnH, radius, radius);

                g2d.setColor(btnBorder);
                g2d.drawRoundRect(btnX, btnY, btnW, btnH, radius, radius);

                g2d.setColor(btnText);
                g2d.setFont(btnFont);
                fm = g2d.getFontMetrics();
                int textW = fm.stringWidth(buttons[i]);
                int textH = fm.getAscent();
                g2d.drawString(buttons[i], btnX + (btnW - textW) / 2, btnY + (btnH + textH) / 2 - 5); // 5 is small
                                                                                                      // adjust, maybe
                                                                                                      // scale?
            }
        }

        private void drawOverlay(Graphics2D g2d) {
            StyleManager sm = StyleManager.getInstance();
            int alpha = (int) (overlayAlpha * 180);

            // Dim background
            g2d.setColor(new Color(0, 0, 0, alpha));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            int panelWidth = scale(sm.getInt(".overlay-panel", "width", 700));
            int panelHeight = scale(sm.getInt(".overlay-panel", "height", 500));
            int panelX = (getWidth() - panelWidth) / 2;
            int panelY = (getHeight() - panelHeight) / 2;

            // Apply fade animation offset
            int offsetY = (int) ((1 - overlayAlpha) * scale(30));
            panelY += offsetY;

            // Panel background with gradient
            Color bgTop = sm.getColor(".overlay-panel", "background-color", new Color(40, 40, 50, 240));
            Color bgBottom = sm.getColor(".overlay-panel", "background-color-bottom", new Color(25, 25, 35, 250));
            GradientPaint gradient = new GradientPaint(
                    panelX, panelY,
                    new Color(bgTop.getRed(), bgTop.getGreen(), bgTop.getBlue(),
                            (int) (overlayAlpha * bgTop.getAlpha())),
                    panelX, panelY + panelHeight, new Color(bgBottom.getRed(), bgBottom.getGreen(), bgBottom.getBlue(),
                            (int) (overlayAlpha * bgBottom.getAlpha())));
            g2d.setPaint(gradient);
            int borderRadius = scale(sm.getInt(".overlay-panel", "border-radius", 20));
            g2d.fillRoundRect(panelX, panelY, panelWidth, panelHeight, borderRadius, borderRadius);

            // Panel border with glow effect
            Color borderColor = sm.getColor(".overlay-panel", "border-color", new Color(100, 150, 255, 100));
            g2d.setColor(new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(),
                    (int) (overlayAlpha * borderColor.getAlpha())));
            int borderWidth = scale(sm.getInt(".overlay-panel", "border-width", 3));
            g2d.setStroke(new BasicStroke(borderWidth));
            g2d.drawRoundRect(panelX, panelY, panelWidth, panelHeight, borderRadius, borderRadius);

            Color glowColor = sm.getColor(".overlay-panel", "border-glow", new Color(150, 180, 255, 60));
            g2d.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(),
                    (int) (overlayAlpha * glowColor.getAlpha())));
            g2d.setStroke(new BasicStroke(1));
            g2d.drawRoundRect(panelX - 2, panelY - 2, panelWidth + 4, panelHeight + 4, borderRadius + 2,
                    borderRadius + 2);

            // Title
            String title = "";
            switch (currentOverlay) {
                case HISTORY:
                    title = "History";
                    break;
                case SAVE:
                    title = "Save Game";
                    break;
                case LOAD:
                    title = "Load Game";
                    break;
                case SETTINGS:
                    title = "Settings";
                    break;
                default:
                    break;
            }

            Font titleFont = scale(sm.getFont(".overlay-title", Font.BOLD, 28));
            g2d.setFont(titleFont);
            Color titleColor = sm.getColor(".overlay-title", "text-color", Color.WHITE);
            g2d.setColor(new Color(titleColor.getRed(), titleColor.getGreen(), titleColor.getBlue(),
                    (int) (overlayAlpha * 255)));
            FontMetrics fm = g2d.getFontMetrics();
            int titleWidth = fm.stringWidth(title);
            g2d.drawString(title, panelX + (panelWidth - titleWidth) / 2, panelY + scale(45));

            // Close button
            int closeW = scale(sm.getInt(".overlay-close", "width", 35));
            int closeH = scale(sm.getInt(".overlay-close", "height", 35));
            int closeX = panelX + panelWidth - closeW - scale(15);
            int closeY = panelY + scale(15);
            Color closeBg = sm.getColor(".overlay-close", "background-color", new Color(255, 100, 100, 200));
            g2d.setColor(new Color(closeBg.getRed(), closeBg.getGreen(), closeBg.getBlue(),
                    (int) (overlayAlpha * closeBg.getAlpha())));
            int closeRadius = scale(sm.getInt(".overlay-close", "border-radius", 8));
            g2d.fillRoundRect(closeX, closeY, closeW, closeH, closeRadius, closeRadius);
            Color closeTextColor = sm.getColor(".overlay-close", "text-color", Color.WHITE);
            g2d.setColor(new Color(closeTextColor.getRed(), closeTextColor.getGreen(), closeTextColor.getBlue(),
                    (int) (overlayAlpha * 255)));
            g2d.setFont(scale(new Font("SansSerif", Font.BOLD, 20)));
            g2d.drawString("✕", closeX + scale(10), closeY + scale(25));

            // Draw content based on overlay type
            if (currentOverlay == OverlayState.HISTORY) {
                drawHistoryContent(g2d, panelX, panelY, panelWidth, panelHeight);
            } else if (currentOverlay == OverlayState.SAVE || currentOverlay == OverlayState.LOAD) {
                drawSaveLoadContent(g2d, panelX, panelY, panelWidth, panelHeight);
            } else if (currentOverlay == OverlayState.SETTINGS) {
                drawSettingsContent(g2d, panelX, panelY, panelWidth, panelHeight);
            }
        }

        private void drawHistoryContent(Graphics2D g2d, int panelX, int panelY, int panelWidth, int panelHeight) {
            StyleManager sm = StyleManager.getInstance();

            // Create clipping region for scrollable content
            int contentX = panelX + scale(20);
            int contentY = panelY + scale(70);
            int contentWidth = panelWidth - scale(40);
            int contentHeight = panelHeight - scale(90);

            Shape oldClip = g2d.getClip();
            g2d.setClip(contentX, contentY, contentWidth, contentHeight);

            List<LogEntry> backlog = engine.getBacklog();
            int entryHeight = scale(sm.getInt(".history-entry", "height", 80));
            int totalHeight = backlog.size() * entryHeight;
            maxHistoryScroll = Math.max(0, totalHeight - contentHeight);

            int startY = contentY - historyScrollOffset;
            int currentY = startY;

            g2d.setFont(scale(sm.getFont(".history-entry", Font.PLAIN, 18)));
            Font nameFont = scale(sm.getFont(".history-entry-name", Font.BOLD, 18));
            Color nameColor = sm.getColor(".history-entry-name", "text-color", new Color(255, 200, 100));
            Color textColor = sm.getColor(".history-entry", "text-color", Color.WHITE);

            int historyIndex = 0;
            for (LogEntry entry : backlog) {
                if (currentY + entryHeight > contentY && currentY < contentY + contentHeight) {
                    if (historyIndex % 2 == 0) {
                        g2d.setColor(new Color(255, 255, 255, 10));
                        g2d.fillRect(contentX, currentY, contentWidth, entryHeight);
                    }

                    int textPadX = scale(10);
                    int textPadY = scale(25);

                    if (entry.speaker != null) {
                        g2d.setFont(nameFont);
                        g2d.setColor(nameColor);
                        g2d.drawString(entry.speaker, contentX + textPadX, currentY + textPadY);

                        g2d.setFont(scale(sm.getFont(".history-entry", Font.PLAIN, 18)));
                        g2d.setColor(textColor);
                        g2d.drawString(entry.text, contentX + textPadX + scale(150), currentY + textPadY);
                    } else {
                        g2d.setFont(scale(sm.getFont(".history-entry", Font.PLAIN, 18)));
                        g2d.setColor(textColor);
                        g2d.drawString(entry.text, contentX + textPadX, currentY + textPadY);
                    }
                }
                currentY += entryHeight;
                historyIndex++;
            }

            g2d.setClip(oldClip);

            // Scrollbar (simplified)
            if (maxHistoryScroll > 0) {
                int scrollBarH = contentHeight * contentHeight / totalHeight;
                int scrollBarY = contentY + (historyScrollOffset * (contentHeight - scrollBarH) / maxHistoryScroll);
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.fillRect(contentX + contentWidth - 5, contentY, 5, contentHeight);
                g2d.setColor(new Color(255, 255, 255, 150));
                g2d.fillRect(contentX + contentWidth - 5, scrollBarY, 5, scrollBarH);
            }
        }

        private void drawSaveLoadContent(Graphics2D g2d, int panelX, int panelY, int panelWidth, int panelHeight) {
            StyleManager sm = StyleManager.getInstance();
            int slotWidth = scale(sm.getInt(".save-slot", "width", 200));
            int slotHeight = scale(sm.getInt(".save-slot", "height", 120));
            int cols = 3;
            int startX = panelX + scale(40);
            int startY = panelY + scale(80);
            int gapX = scale(20);
            int gapY = scale(20);

            for (int i = 0; i < 9; i++) {
                int col = i % cols;
                int row = i / cols;
                int slotX = startX + col * (slotWidth + gapX);
                int slotY = startY + row * (slotHeight + gapY);
                int slotNum = i + 1;

                SaveData data = SaveManager.load(slotNum);

                // Slot Background
                if (hoveredSlot == slotNum) {
                    g2d.setColor(sm.getColor(".save-slot", "hover-background-color", new Color(80, 80, 100, 200)));
                } else {
                    g2d.setColor(sm.getColor(".save-slot", "background-color", new Color(60, 60, 80, 180)));
                }
                g2d.fillRoundRect(slotX, slotY, slotWidth, slotHeight, 10, 10);

                g2d.setColor(sm.getColor(".save-slot", "border-color", new Color(150, 150, 200)));
                g2d.drawRoundRect(slotX, slotY, slotWidth, slotHeight, 10, 10);

                // Slot Content
                g2d.setColor(Color.WHITE);
                g2d.setFont(scale(new Font("SansSerif", Font.BOLD, 14)));
                g2d.drawString("Slot " + slotNum, slotX + scale(10), slotY + scale(20));

                if (data != null) {
                    g2d.setFont(scale(new Font("SansSerif", Font.PLAIN, 12)));
                    g2d.drawString(data.timestamp.substring(0, Math.min(data.timestamp.length(), 16)),
                            slotX + scale(10),
                            slotY + scale(40));
                    g2d.setColor(Color.LIGHT_GRAY);

                    // Draw thumbnail or description
                    if (data.description != null) {
                        g2d.drawString(data.description, slotX + scale(10), slotY + scale(60));
                    }

                    // Delete button (X)
                    g2d.setColor(new Color(255, 80, 80));
                    g2d.fillOval(slotX + slotWidth - scale(25), slotY + scale(35), scale(20), scale(20));
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(scale(new Font("SansSerif", Font.BOLD, 12)));
                    g2d.drawString("X", slotX + slotWidth - scale(19), slotY + scale(49));
                } else {
                    g2d.setColor(Color.GRAY);
                    g2d.drawString("Empty", slotX + scale(10), slotY + scale(60));
                }
            }

            // Hint text
            Font hintFont = scale(sm.getFont(".hint-text", Font.ITALIC, 12));
            g2d.setFont(hintFont);
            Color hintColor = sm.getColor(".hint-text", "text-color", new Color(180, 180, 180, 180));
            g2d.setColor(new Color(hintColor.getRed(), hintColor.getGreen(), hintColor.getBlue(),
                    (int) (overlayAlpha * hintColor.getAlpha())));
            String hint = currentOverlay == OverlayState.SAVE ? "Click a slot to save • Press ESC to close"
                    : "Click a slot to load • Press ESC to close";
            g2d.drawString(hint, panelX + scale(20), panelY + panelHeight - scale(15));
        }

        private void drawSaveLoadContent_UNUSED(Graphics2D g2d, int panelX, int panelY, int panelWidth,
                int panelHeight) {
            StyleManager sm = StyleManager.getInstance();

            int slotWidth = sm.getInt(".save-slot", "width", 200);
            int slotHeight = sm.getInt(".save-slot", "height", 120);
            int slotRadius = sm.getInt(".save-slot", "border-radius", 12);
            int cols = 3;
            int startX = panelX + 40;
            int startY = panelY + 80;
            int gapX = 20;
            int gapY = 20;

            for (int i = 0; i < 9; i++) {
                int slot = i + 1;
                int col = i % cols;
                int row = i / cols;
                int slotX = startX + col * (slotWidth + gapX);
                int slotY = startY + row * (slotHeight + gapY);

                // Check if save exists
                SaveData data = SaveManager.load(slot);
                boolean hasData = data != null;

                // Slot background
                boolean isHovered = (slot == hoveredSlot);
                Color bgColor;
                if (isHovered) {
                    bgColor = sm.getColor(".save-slot-hover", "background-color", new Color(80, 120, 180, 200));
                } else if (hasData) {
                    bgColor = sm.getColor(".save-slot-filled", "background-color", new Color(60, 80, 100, 180));
                } else {
                    bgColor = sm.getColor(".save-slot-empty", "background-color", new Color(50, 50, 60, 150));
                }
                g2d.setColor(new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(),
                        (int) (overlayAlpha * bgColor.getAlpha())));
                g2d.fillRoundRect(slotX, slotY, slotWidth, slotHeight, slotRadius, slotRadius);

                // Slot border
                Color borderColor;
                int borderWidth;
                if (isHovered) {
                    borderColor = sm.getColor(".save-slot-hover", "border-color", new Color(100, 180, 255, 255));
                    borderWidth = sm.getInt(".save-slot-hover", "border-width", 2);
                } else if (hasData) {
                    borderColor = sm.getColor(".save-slot-filled", "border-color", new Color(100, 100, 120, 150));
                    borderWidth = 1;
                } else {
                    borderColor = sm.getColor(".save-slot-empty", "border-color", new Color(100, 100, 120, 150));
                    borderWidth = 1;
                }
                g2d.setColor(new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(),
                        (int) (overlayAlpha * borderColor.getAlpha())));
                g2d.setStroke(new BasicStroke(borderWidth));
                g2d.drawRoundRect(slotX, slotY, slotWidth, slotHeight, slotRadius, slotRadius);

                // Slot number
                g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
                Color slotTextColor = hasData ? sm.getColor(".save-slot-filled", "text-color", Color.WHITE)
                        : sm.getColor(".save-slot-empty", "text-color", new Color(150, 150, 150, 180));
                g2d.setColor(new Color(slotTextColor.getRed(), slotTextColor.getGreen(), slotTextColor.getBlue(),
                        (int) (overlayAlpha * 255)));
                g2d.drawString("Slot " + slot, slotX + 15, slotY + 30);

                if (hasData) {
                    // Save description
                    String desc = data.description;
                    if (desc != null && desc.length() > 25) {
                        desc = desc.substring(0, 22) + "...";
                    }

                    g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
                    Color descColor = sm.getColor(".save-slot-filled", "description-color",
                            new Color(200, 200, 200, 220));
                    g2d.setColor(new Color(descColor.getRed(), descColor.getGreen(), descColor.getBlue(),
                            (int) (overlayAlpha * descColor.getAlpha())));
                    if (desc != null) {
                        g2d.drawString(desc, slotX + 15, slotY + 55);
                    }

                    // Step info
                    Color stepColor = sm.getColor(".save-slot-filled", "step-color", new Color(150, 200, 150, 200));
                    g2d.setColor(new Color(stepColor.getRed(), stepColor.getGreen(), stepColor.getBlue(),
                            (int) (overlayAlpha * stepColor.getAlpha())));
                    g2d.drawString("Step: " + data.stepIndex, slotX + 15, slotY + 80);

                    // Saved indicator
                    Color indicatorColor = sm.getColor(".save-slot-filled", "indicator-color",
                            new Color(100, 200, 100, 255));
                    g2d.setColor(new Color(indicatorColor.getRed(), indicatorColor.getGreen(), indicatorColor.getBlue(),
                            (int) (overlayAlpha * 255)));
                    g2d.fillOval(slotX + slotWidth - 25, slotY + 10, 10, 10);

                    // Delete button
                    int delSize = 20;
                    int delX = slotX + slotWidth - delSize - 5;
                    int delY = slotY + 35; // Position below indicator

                    g2d.setColor(new Color(255, 80, 80, (int) (overlayAlpha * 200)));
                    g2d.fillRoundRect(delX, delY, delSize, delSize, 5, 5);

                    g2d.setColor(new Color(255, 255, 255, (int) (overlayAlpha * 255)));
                    g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
                    FontMetrics fmDel = g2d.getFontMetrics();
                    g2d.drawString("x", delX + (delSize - fmDel.stringWidth("x")) / 2,
                            delY + (delSize + fmDel.getAscent()) / 2 - 2);
                } else {
                    // Empty slot
                    g2d.setFont(new Font("SansSerif", Font.ITALIC, 14));
                    Color emptyColor = sm.getColor(".save-slot-empty", "text-color", new Color(150, 150, 150, 180));
                    g2d.setColor(new Color(emptyColor.getRed(), emptyColor.getGreen(), emptyColor.getBlue(),
                            (int) (overlayAlpha * emptyColor.getAlpha())));
                    g2d.drawString("Empty", slotX + 15, slotY + 70);
                }
            }

            // Hint text
            Font hintFont = sm.getFont(".hint-text", Font.ITALIC, 12);
            g2d.setFont(hintFont);
            Color hintColor = sm.getColor(".hint-text", "text-color", new Color(180, 180, 180, 180));
            g2d.setColor(new Color(hintColor.getRed(), hintColor.getGreen(), hintColor.getBlue(),
                    (int) (overlayAlpha * hintColor.getAlpha())));
            String hint = currentOverlay == OverlayState.SAVE ? "Click a slot to save • Press ESC to close"
                    : "Click a slot to load • Press ESC to close";
            g2d.drawString(hint, panelX + 20, panelY + panelHeight - 15);

        }

        private void drawSettingsContent(Graphics2D g2d, int panelX, int panelY, int panelWidth, int panelHeight) {
            SettingsManager sm = SettingsManager.getInstance();
            StyleManager style = StyleManager.getInstance();

            int startY = panelY + scale(100);
            int gapY = scale(70);
            int sliderWidth = scale(300);
            int sliderHeight = scale(20);
            int labelWidth = scale(150);

            int contentX = panelX + (panelWidth - (labelWidth + sliderWidth + scale(20))) / 2;

            Font font = scale(style.getFont(".options", Font.BOLD, 22));
            g2d.setFont(font);

            Color textColor = style.getColor(".options", "text-color", Color.WHITE);
            Color barColor = new Color(100, 100, 100);
            Color fillColor = new Color(100, 200, 255);

            // Dim down text color for alpha
            int alpha = (int) (overlayAlpha * 255);
            textColor = new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), alpha);
            barColor = new Color(barColor.getRed(), barColor.getGreen(), barColor.getBlue(), alpha);
            fillColor = new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), alpha);

            // 1. Music Volume
            int rowY = startY;
            drawOptionSlider(g2d, "Music Volume", sm.getMusicVolume(), rowY, contentX, labelWidth, sliderWidth,
                    sliderHeight, textColor, barColor, fillColor);

            // 2. SFX Volume
            rowY += gapY;
            drawOptionSlider(g2d, "SFX Volume", sm.getSfxVolume(), rowY, contentX, labelWidth, sliderWidth,
                    sliderHeight, textColor, barColor, fillColor);

            // 3. Text Speed
            rowY += gapY;
            // Normalize speed 0.1..3.0 to 0..1
            float normSpeed = (sm.getTextSpeed() - 0.1f) / 2.9f;
            drawOptionSlider(g2d, "Text Speed", normSpeed, rowY, contentX, labelWidth, sliderWidth, sliderHeight,
                    textColor, barColor, fillColor);

            // 4. Fullscreen
            rowY += gapY;
            g2d.setColor(textColor);
            g2d.drawString("Fullscreen", contentX, rowY + scale(22));

            int checkX = contentX + labelWidth + scale(20);
            g2d.setColor(barColor);
            g2d.drawRect(checkX, rowY, scale(30), scale(30));
            if (sm.isFullscreen()) {
                g2d.setColor(fillColor);
                g2d.fillRect(checkX + scale(5), rowY + scale(5), scale(20), scale(20));
            }
        }

        private void drawOptionSlider(Graphics2D g2d, String label, float val, int y, int startX, int labelW,
                int sliderW, int sliderH, Color textC, Color barC, Color fillC) {
            g2d.setColor(textC);
            g2d.drawString(label, startX, y + scale(18));

            int sliderX = startX + labelW + scale(20);
            g2d.setColor(barC);
            g2d.fillRect(sliderX, y, sliderW, sliderH);

            val = Math.max(0f, Math.min(1f, val));
            int fillW = (int) (val * sliderW);
            g2d.setColor(fillC);
            g2d.fillRect(sliderX, y, fillW, sliderH);

            // Knob
            g2d.setColor(Color.WHITE);
            // Need alpha for white too if we want fading
            int knobW = scale(16);
            int knobH = scale(28);
            g2d.fillOval(sliderX + fillW - knobW / 2, y - scale(4), knobW, knobH);
        }

        private void drawToolbarButton(Graphics2D g, String text, int x, int y, int w, int h) {
            StyleManager sm = StyleManager.getInstance();

            Color bgColor = sm.getColor(".toolbar-button", "background-color", new Color(100, 100, 100, 150));
            g.setColor(bgColor);
            int radius = scale(sm.getInt(".toolbar-button", "border-radius", 8));
            g.fillRoundRect(x, y, w, h, radius, radius);

            Color borderColor = sm.getColor(".toolbar-button", "border-color", new Color(150, 150, 150, 100));
            g.setColor(borderColor);
            g.drawRoundRect(x, y, w, h, radius, radius);

            Color textColor = sm.getColor(".toolbar-button", "text-color", Color.WHITE);
            g.setColor(textColor);
            Font btnFont = scale(sm.getFont(".toolbar-button", Font.BOLD, 12));
            g.setFont(btnFont);
            FontMetrics fm = g.getFontMetrics();
            int tw = fm.stringWidth(text);
            g.drawString(text, x + (w - tw) / 2, y + scale(20));
        }

        private void drawDialogueBox(Graphics2D g2) {
            StyleManager sm = StyleManager.getInstance();

            int boxHeight = scale(200);
            int boxY = getHeight() - boxHeight - scale(20);
            int boxX = scale(20);
            int boxWidth = getWidth() - scale(40);

            Point customPos = engine.getCustomDialogPosition();
            if (customPos != null) {
                boxX = scale(customPos.x);
                boxY = scale(customPos.y);
            }

            int borderRadius = scale(sm.getInt(".dialog-box", "border-radius", 20));
            int borderWidth = scale(sm.getInt(".dialog-box", "border-width", 2));

            Color bgColor = sm.getColor(".dialog-box", "background-color", Color.BLACK);
            int opacity = sm.getInt(".dialog-box", "opacity", 200);
            g2.setColor(new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), opacity));
            g2.fillRoundRect(boxX, boxY, boxWidth, boxHeight, borderRadius, borderRadius);

            Color borderColor = sm.getColor(".dialog-box", "border-color", new Color(255, 255, 255, 100));
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(borderWidth));
            g2.drawRoundRect(boxX, boxY, boxWidth, boxHeight, borderRadius, borderRadius);
        }

        public int getOptionAt(int x, int y) {
            String[] options = engine.getCurrentOptions();
            if (options == null)
                return -1;

            int btnWidth = scale(600);
            int btnHeight = scale(60);
            int startY = (getHeight() - (options.length * (btnHeight + scale(20)))) / 2;
            int startX = (getWidth() - btnWidth) / 2;

            for (int i = 0; i < options.length; i++) {
                int btnY = startY + i * (btnHeight + scale(20));
                if (x >= startX && x <= startX + btnWidth && y >= btnY && y <= btnY + btnHeight) {
                    return i;
                }
            }
            return -1;
        }
    }
}
