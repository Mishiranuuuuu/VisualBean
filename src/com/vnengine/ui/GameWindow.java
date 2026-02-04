package com.vnengine.ui;

import com.vnengine.core.GameEngine;
import com.vnengine.core.SaveManager;
import com.vnengine.core.SaveData;
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
        NONE, HISTORY, SAVE, LOAD
    }

    private OverlayState currentOverlay = OverlayState.NONE;

    // For history scrolling
    private int historyScrollOffset = 0;
    private int maxHistoryScroll = 0;

    // For save/load slot hover
    private int hoveredSlot = -1;

    // Animation for overlay fade
    private float overlayAlpha = 0f;
    private OverlayState targetOverlay = OverlayState.NONE;

    public void setFullscreen(boolean fullscreen) {
        if (fullscreen == isUndecorated())
            return; // Already in desired state (approximated check)

        dispose(); // Must dispose before changing decoration style

        if (fullscreen) {
            setUndecorated(true);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            if (gd.isFullScreenSupported()) {
                gd.setFullScreenWindow(this);
            } else {
                setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
        } else {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            gd.setFullScreenWindow(null); // Exit full screen exclusive

            setUndecorated(false);
            setExtendedState(JFrame.NORMAL);
            setSize(1280, 720); // Default size restore or keep previous?
            setLocationRelativeTo(null);
        }

        setVisible(true);
    }

    public GameWindow(GameEngine engine) {
        this.engine = engine;
        setTitle("Java Visual Novel Engine");
        setSize(1280, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        panel = new GamePanel();
        panel.setFocusable(true);
        panel.requestFocusInWindow();

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

                int tbW = 80;
                int tbH = 30;
                int tbY = 10;
                int histX = getWidth() - 100;
                int saveX = histX - tbW - 10;
                int loadX = saveX - tbW - 10;

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
        int overlayWidth = sm.getInt(".overlay-panel", "width", 700);
        int overlayHeight = sm.getInt(".overlay-panel", "height", 500);
        int overlayX = (panel.getWidth() - overlayWidth) / 2;
        int overlayY = (panel.getHeight() - overlayHeight) / 2;

        int slotWidth = sm.getInt(".save-slot", "width", 200);
        int slotHeight = sm.getInt(".save-slot", "height", 120);
        int cols = 3;
        int startX = overlayX + 40;
        int startY = overlayY + 80;
        int gapX = 20;
        int gapY = 20;

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

        int btnW = sm.getInt(".main-menu-button", "width", 300);
        int btnH = sm.getInt(".main-menu-button", "height", 60);
        int startY = h / 2;
        int gap = sm.getInt(".main-menu-button", "gap", 30);

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

        // Exit
        int exitY = loadY + btnH + gap;
        if (mx >= btnX && mx <= btnX + btnW && my >= exitY && my <= exitY + btnH) {
            System.exit(0);
        }
    }

    private void handleOverlayClick(MouseEvent e) {
        StyleManager sm = StyleManager.getInstance();
        int overlayWidth = sm.getInt(".overlay-panel", "width", 700);
        int overlayHeight = sm.getInt(".overlay-panel", "height", 500);
        int overlayX = (panel.getWidth() - overlayWidth) / 2;
        int overlayY = (panel.getHeight() - overlayHeight) / 2;

        // Check close button (top right of panel)
        int closeW = sm.getInt(".overlay-close", "width", 35);
        int closeH = sm.getInt(".overlay-close", "height", 35);
        int closeX = overlayX + overlayWidth - closeW - 15;
        int closeY = overlayY + 15;
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

        // Save/Load slot click
        if (currentOverlay == OverlayState.SAVE || currentOverlay == OverlayState.LOAD) {

            // Check delete button clicks
            int slotWidth = sm.getInt(".save-slot", "width", 200);
            int slotHeight = sm.getInt(".save-slot", "height", 120);
            int cols = 3;
            int startX = overlayX + 40;
            int startY = overlayY + 80;
            int gapX = 20;
            int gapY = 20;

            for (int i = 0; i < 9; i++) {
                int col = i % cols;
                int row = i / cols;
                int slotX = startX + col * (slotWidth + gapX);
                int slotY = startY + row * (slotHeight + gapY);

                SaveData data = SaveManager.load(i + 1);
                if (data != null) {
                    int delSize = 20;
                    int delX = slotX + slotWidth - delSize - 5;
                    int delY = slotY + 35;

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

            int totalWidth = characters.size() * 300;
            int charStartX = (getWidth() - totalWidth) / 2 + 50;
            int xOffset = charStartX;

            for (Map.Entry<String, String> entry : characters.entrySet()) {
                String name = entry.getKey();
                String spriteName = entry.getValue();
                BufferedImage sprite = loadImage(spriteName);

                int drawX = xOffset;
                int drawY = 150;

                Point customPos = engine.getCharacterPosition(name);
                if (customPos != null) {
                    drawX = customPos.x;
                    drawY = customPos.y;
                } else {
                    xOffset += 300;
                }

                if (sprite != null) {
                    double scale = engine.getCharacterScale(name);
                    int h = (int) (500 * scale);
                    int w = (int) ((double) sprite.getWidth() / sprite.getHeight() * h);
                    g2d.drawImage(sprite, drawX, drawY, w, h, null);
                } else {
                    g2d.setColor(Color.BLUE);
                    g2d.fillRect(drawX, drawY, 200, 400);
                    g2d.setColor(Color.WHITE);
                    g2d.drawString(name, drawX + 50, drawY + 50);
                }
            }

            // Draw Dialogue Box Content
            if (uiVisible && currentOverlay == OverlayState.NONE) {
                drawDialogueBox(g2d);

                String speaker = engine.getCurrentSpeaker();
                String text = engine.getCurrentDialogue();
                String[] options = engine.getCurrentOptions();
                StyleManager sm = StyleManager.getInstance();

                int boxHeight = 200;
                int boxY = getHeight() - boxHeight - 20;
                int boxX = 20;
                Point dialogPos = engine.getCustomDialogPosition();
                if (dialogPos != null) {
                    boxX = dialogPos.x;
                    boxY = dialogPos.y;
                }

                // Get padding from CSS
                int paddingLeft = sm.getInt(".dialog-box", "padding-left", 20);
                int paddingTop = sm.getInt(".dialog-box", "padding-top", 20);
                int nameOffsetY = sm.getInt(".dialog-box", "name-offset-y", 25);
                int textOffsetY = sm.getInt(".dialog-box", "text-offset-y", 55);

                int textX = boxX + paddingLeft;
                int nameY = boxY + paddingTop + nameOffsetY;
                int textY = boxY + paddingTop + textOffsetY;

                if (options != null) {
                    g2d.setColor(new Color(0, 0, 0, 150));
                    g2d.fillRect(0, 0, getWidth(), getHeight());

                    int btnWidth = sm.getInt(".menu-button", "width", 600);
                    int btnHeight = sm.getInt(".menu-button", "height", 60);
                    int btnRadius = sm.getInt(".menu-button", "border-radius", 10);
                    int startY = (getHeight() - (options.length * (btnHeight + 20))) / 2;
                    int optStartX = (getWidth() - btnWidth) / 2;

                    Font btnFont = sm.getFont(".menu-button", Font.BOLD, 24);
                    g2d.setFont(btnFont);

                    Color btnBg = sm.getColor(".menu-button", "background-color", new Color(50, 50, 50));
                    Color btnText = sm.getColor(".menu-button", "text-color", Color.WHITE);
                    Color btnBorder = sm.getColor(".menu-button", "border-color", Color.WHITE);

                    for (int i = 0; i < options.length; i++) {
                        int y = startY + i * (btnHeight + 20);

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
                    int tbW = sm.getInt(".toolbar-button", "width", 80);
                    int tbH = sm.getInt(".toolbar-button", "height", 30);
                    int tbY = 10;

                    int histX = getWidth() - tbW - 20;
                    drawToolbarButton(g2d, "History", histX, tbY, tbW, tbH);

                    int saveX = histX - tbW - 10;
                    drawToolbarButton(g2d, "Save", saveX, tbY, tbW, tbH);

                    int loadX = saveX - tbW - 10;
                    drawToolbarButton(g2d, "Load", loadX, tbY, tbW, tbH);

                    // Draw speaker name
                    if (speaker != null) {
                        int nameFontSize = sm.getInt(".dialog-box", "name-font-size", 28);
                        g2d.setFont(new Font(sm.getString(".dialog-box", "font-family", "SansSerif"), Font.BOLD,
                                nameFontSize));
                        Color nameColor = sm.getColor(".dialog-box", "name-color", new Color(255, 200, 100));
                        g2d.setColor(nameColor);
                        g2d.drawString(speaker, textX, nameY);
                    }

                    // Draw dialogue text using renderer
                    int maxWidth = getWidth() - boxX - paddingLeft - sm.getInt(".dialog-box", "padding-right", 40);
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

            Font titleFont = sm.getFont(".main-menu-title", Font.BOLD, 60);
            g2d.setFont(titleFont);
            Color titleColor = sm.getColor(".main-menu-title", "text-color", new Color(220, 220, 255));
            g2d.setColor(titleColor);

            FontMetrics fm = g2d.getFontMetrics();
            int titleW = fm.stringWidth(title);

            float titleYRatio = sm.getFloat(".main-menu-title", "y-position-ratio", 0.33f);
            int titleY = (int) (h * titleYRatio);
            g2d.drawString(title, (w - titleW) / 2, titleY);

            // Buttons
            String[] buttons = { "New Game", "Load Game", "Exit" };
            int btnW = sm.getInt(".main-menu-button", "width", 300);
            int btnH = sm.getInt(".main-menu-button", "height", 60);
            int gap = sm.getInt(".main-menu-button", "gap", 30);
            int radius = sm.getInt(".main-menu-button", "border-radius", 15);

            Color btnBg = sm.getColor(".main-menu-button", "background-color", new Color(50, 50, 70));
            Color btnBorder = sm.getColor(".main-menu-button", "border-color", new Color(100, 100, 150));
            Color btnText = sm.getColor(".main-menu-button", "text-color", Color.WHITE);
            Font btnFont = sm.getFont(".main-menu-button", Font.PLAIN, 24);

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
                g2d.drawString(buttons[i], btnX + (btnW - textW) / 2, btnY + (btnH + textH) / 2 - 5);
            }
        }

        private void drawOverlay(Graphics2D g2d) {
            StyleManager sm = StyleManager.getInstance();
            int alpha = (int) (overlayAlpha * 180);

            // Dim background
            g2d.setColor(new Color(0, 0, 0, alpha));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            int panelWidth = sm.getInt(".overlay-panel", "width", 700);
            int panelHeight = sm.getInt(".overlay-panel", "height", 500);
            int panelX = (getWidth() - panelWidth) / 2;
            int panelY = (getHeight() - panelHeight) / 2;

            // Apply fade animation offset
            int offsetY = (int) ((1 - overlayAlpha) * 30);
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
            int borderRadius = sm.getInt(".overlay-panel", "border-radius", 20);
            g2d.fillRoundRect(panelX, panelY, panelWidth, panelHeight, borderRadius, borderRadius);

            // Panel border with glow effect
            Color borderColor = sm.getColor(".overlay-panel", "border-color", new Color(100, 150, 255, 100));
            g2d.setColor(new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(),
                    (int) (overlayAlpha * borderColor.getAlpha())));
            int borderWidth = sm.getInt(".overlay-panel", "border-width", 3);
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
                default:
                    break;
            }

            Font titleFont = sm.getFont(".overlay-title", Font.BOLD, 28);
            g2d.setFont(titleFont);
            Color titleColor = sm.getColor(".overlay-title", "text-color", Color.WHITE);
            g2d.setColor(new Color(titleColor.getRed(), titleColor.getGreen(), titleColor.getBlue(),
                    (int) (overlayAlpha * 255)));
            FontMetrics fm = g2d.getFontMetrics();
            int titleWidth = fm.stringWidth(title);
            g2d.drawString(title, panelX + (panelWidth - titleWidth) / 2, panelY + 45);

            // Close button
            int closeW = sm.getInt(".overlay-close", "width", 35);
            int closeH = sm.getInt(".overlay-close", "height", 35);
            int closeX = panelX + panelWidth - closeW - 15;
            int closeY = panelY + 15;
            Color closeBg = sm.getColor(".overlay-close", "background-color", new Color(255, 100, 100, 200));
            g2d.setColor(new Color(closeBg.getRed(), closeBg.getGreen(), closeBg.getBlue(),
                    (int) (overlayAlpha * closeBg.getAlpha())));
            int closeRadius = sm.getInt(".overlay-close", "border-radius", 8);
            g2d.fillRoundRect(closeX, closeY, closeW, closeH, closeRadius, closeRadius);
            Color closeTextColor = sm.getColor(".overlay-close", "text-color", Color.WHITE);
            g2d.setColor(new Color(closeTextColor.getRed(), closeTextColor.getGreen(), closeTextColor.getBlue(),
                    (int) (overlayAlpha * 255)));
            g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
            g2d.drawString("✕", closeX + 10, closeY + 25);

            // Draw content based on overlay type
            if (currentOverlay == OverlayState.HISTORY) {
                drawHistoryContent(g2d, panelX, panelY, panelWidth, panelHeight);
            } else if (currentOverlay == OverlayState.SAVE || currentOverlay == OverlayState.LOAD) {
                drawSaveLoadContent(g2d, panelX, panelY, panelWidth, panelHeight);
            }
        }

        private void drawHistoryContent(Graphics2D g2d, int panelX, int panelY, int panelWidth, int panelHeight) {
            StyleManager sm = StyleManager.getInstance();

            // Create clipping region for scrollable content
            int contentX = panelX + 20;
            int contentY = panelY + 70;
            int contentWidth = panelWidth - 40;
            int contentHeight = panelHeight - 90;

            Shape oldClip = g2d.getClip();
            g2d.setClip(contentX, contentY, contentWidth, contentHeight);

            List<LogEntry> backlog = engine.getBacklog();
            int entryHeight = sm.getInt(".history-entry", "height", 80);
            int totalHeight = backlog.size() * entryHeight;
            maxHistoryScroll = Math.max(0, totalHeight - contentHeight);

            int startY = contentY - historyScrollOffset;
            int currentY = startY;

            g2d.setFont(sm.getFont(".history-entry", Font.PLAIN, 18));
            Font nameFont = sm.getFont(".history-entry-name", Font.BOLD, 18);
            Color nameColor = sm.getColor(".history-entry-name", "text-color", new Color(255, 200, 100));
            Color textColor = sm.getColor(".history-entry", "text-color", Color.WHITE);

            int historyIndex = 0;
            for (LogEntry entry : backlog) {
                if (currentY + entryHeight > contentY && currentY < contentY + contentHeight) {
                    if (historyIndex % 2 == 0) {
                        g2d.setColor(new Color(255, 255, 255, 10));
                        g2d.fillRect(contentX, currentY, contentWidth, entryHeight);
                    }

                    int textPadX = 10;
                    int textPadY = 25;

                    if (entry.speaker != null) {
                        g2d.setFont(nameFont);
                        g2d.setColor(nameColor);
                        g2d.drawString(entry.speaker, contentX + textPadX, currentY + textPadY);

                        g2d.setFont(sm.getFont(".history-entry", Font.PLAIN, 18));
                        g2d.setColor(textColor);
                        g2d.drawString(entry.text, contentX + textPadX + 150, currentY + textPadY);
                    } else {
                        g2d.setFont(sm.getFont(".history-entry", Font.PLAIN, 18));
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
            int slotWidth = sm.getInt(".save-slot", "width", 200);
            int slotHeight = sm.getInt(".save-slot", "height", 120);
            int cols = 3;
            int startX = panelX + 40;
            int startY = panelY + 80;
            int gapX = 20;
            int gapY = 20;

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
                g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
                g2d.drawString("Slot " + slotNum, slotX + 10, slotY + 20);

                if (data != null) {
                    g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
                    g2d.drawString(data.timestamp.substring(0, Math.min(data.timestamp.length(), 16)), slotX + 10,
                            slotY + 40);
                    g2d.setColor(Color.LIGHT_GRAY);

                    // Draw thumbnail or description
                    if (data.description != null) {
                        g2d.drawString(data.description, slotX + 10, slotY + 60);
                    }

                    // Delete button (X)
                    g2d.setColor(new Color(255, 80, 80));
                    g2d.fillOval(slotX + slotWidth - 25, slotY + 35, 20, 20);
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
                    g2d.drawString("X", slotX + slotWidth - 19, slotY + 49);
                } else {
                    g2d.setColor(Color.GRAY);
                    g2d.drawString("Empty", slotX + 10, slotY + 60);
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

        private void drawToolbarButton(Graphics2D g, String text, int x, int y, int w, int h) {
            StyleManager sm = StyleManager.getInstance();

            Color bgColor = sm.getColor(".toolbar-button", "background-color", new Color(100, 100, 100, 150));
            g.setColor(bgColor);
            int radius = sm.getInt(".toolbar-button", "border-radius", 8);
            g.fillRoundRect(x, y, w, h, radius, radius);

            Color borderColor = sm.getColor(".toolbar-button", "border-color", new Color(150, 150, 150, 100));
            g.setColor(borderColor);
            g.drawRoundRect(x, y, w, h, radius, radius);

            Color textColor = sm.getColor(".toolbar-button", "text-color", Color.WHITE);
            g.setColor(textColor);
            Font btnFont = sm.getFont(".toolbar-button", Font.BOLD, 12);
            g.setFont(btnFont);
            FontMetrics fm = g.getFontMetrics();
            int tw = fm.stringWidth(text);
            g.drawString(text, x + (w - tw) / 2, y + 20);
        }

        private void drawDialogueBox(Graphics2D g2) {
            StyleManager sm = StyleManager.getInstance();

            int boxHeight = 200;
            int boxY = getHeight() - boxHeight - 20;
            int boxX = 20;
            int boxWidth = getWidth() - 40;

            Point customPos = engine.getCustomDialogPosition();
            if (customPos != null) {
                boxX = customPos.x;
                boxY = customPos.y;
            }

            int borderRadius = sm.getInt(".dialog-box", "border-radius", 20);
            int borderWidth = sm.getInt(".dialog-box", "border-width", 2);

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

            int btnWidth = 600;
            int btnHeight = 60;
            int startY = (getHeight() - (options.length * (btnHeight + 20))) / 2;
            int startX = (getWidth() - btnWidth) / 2;

            for (int i = 0; i < options.length; i++) {
                int btnY = startY + i * (btnHeight + 20);
                if (x >= startX && x <= startX + btnWidth && y >= btnY && y <= btnY + btnHeight) {
                    return i;
                }
            }
            return -1;
        }
    }
}
