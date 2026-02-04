package com.vnengine.ui;

import com.vnengine.core.GameEngine;
import com.vnengine.util.Easing;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * A secondary window that can display VN content independently of the main
 * window.
 * It manages its own state (background, characters, text).
 */
public class SubWindow extends JFrame {

    private String id;
    private SubWindowPanel panel;

    // Independent State for this window
    protected String currentBackground = null;
    protected Map<String, String> visibleCharacters = new HashMap<>(); // Name -> ImagePath
    protected Map<String, Double> characterScales = new HashMap<>();
    protected Map<String, Point> characterPositions = new HashMap<>();

    // Text State
    protected String currentSpeaker = null;
    protected String currentText = null;

    // Cache
    private Map<String, BufferedImage> imageCache = new HashMap<>();

    public SubWindow(String id, String title, int width, int height) {
        this.id = id;
        setTitle(title);
        setSize(width, height);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Engine manages lifecycle
        setLocationRelativeTo(null);
        setResizable(false);

        panel = new SubWindowPanel();
        add(panel);

        // Animation loop for this window
        Timer timer = new Timer(16, e -> panel.repaint());
        timer.start();

        setVisible(true);
    }

    // --- State Management Methods ---

    public void setBackground(String imagePath) {
        this.currentBackground = imagePath;
        panel.repaint();
    }

    public void showCharacter(String name, String imagePath, int x, int y, double scale) {
        visibleCharacters.put(name, imagePath);
        if (x != -1 && y != -1) {
            characterPositions.put(name, new Point(x, y));
        }
        characterScales.put(name, scale);
        panel.repaint();
    }

    public void hideCharacter(String name) {
        visibleCharacters.remove(name);
        characterPositions.remove(name);
        characterScales.remove(name);
        panel.repaint();
    }

    public void moveCharacter(String name, int x, int y) {
        if (visibleCharacters.containsKey(name)) {
            characterPositions.put(name, new Point(x, y));
            panel.repaint();
        }
    }

    public void setCharacterScale(String name, double scale) {
        if (visibleCharacters.containsKey(name)) {
            characterScales.put(name, scale);
            panel.repaint();
        }
    }

    public void setText(String speaker, String text) {
        this.currentSpeaker = speaker;
        this.currentText = text;
        panel.repaint();
    }

    public Point getCharacterPosition(String name) {
        return characterPositions.get(name);
    }

    public double getCharacterScale(String name) {
        return characterScales.getOrDefault(name, 1.0);
    }

    // --- Helper Methods ---

    private BufferedImage loadImage(String name) {
        if (name == null)
            return null;
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
            }

            if (file != null) {
                BufferedImage img = ImageIO.read(file);
                imageCache.put(name, img);
                return img;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- Inner Panel Class ---

    private class SubWindowPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // 1. Draw Background
            if (currentBackground != null) {
                if (currentBackground.startsWith("#")) {
                    g2d.setColor(Color.decode(currentBackground));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    BufferedImage bg = loadImage(currentBackground);
                    if (bg != null) {
                        double panelRatio = (double) getWidth() / getHeight();
                        double imgRatio = (double) bg.getWidth() / bg.getHeight();
                        int drawW, drawH, drawX, drawY;

                        if (panelRatio > imgRatio) {
                            drawW = getWidth();
                            drawH = (int) (drawW / imgRatio);
                            drawX = 0;
                            drawY = (getHeight() - drawH) / 2;
                        } else {
                            drawH = getHeight();
                            drawW = (int) (drawH * imgRatio);
                            drawX = (getWidth() - drawW) / 2;
                            drawY = 0;
                        }
                        g2d.drawImage(bg, drawX, drawY, drawW, drawH, null);
                    }
                }
            } else {
                g2d.setColor(Color.BLACK);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }

            // 2. Draw Characters
            // Sort or just iterate (Z-order usually insertion order for maps)
            // Ideally we'd have a Z-index, but simple map iteration for now

            // Auto-layout logic for unspecified positions (simplified version of
            // GameWindow)
            int totalWidth = visibleCharacters.size() * 300;
            int charStartX = (getWidth() - totalWidth) / 2 + 50;
            int xOffset = charStartX;

            for (Map.Entry<String, String> entry : visibleCharacters.entrySet()) {
                String name = entry.getKey();
                String spriteName = entry.getValue();
                BufferedImage sprite = loadImage(spriteName);

                int drawX = xOffset;
                int drawY = 150;

                Point customPos = characterPositions.get(name);
                if (customPos != null) {
                    drawX = customPos.x;
                    drawY = customPos.y;
                } else {
                    xOffset += 300;
                }

                if (sprite != null) {
                    double scale = getCharacterScale(name);
                    int h = (int) (500 * scale);
                    int w = (int) ((double) sprite.getWidth() / sprite.getHeight() * h);

                    // Simple centering/bottom align if needed, but using top-left draw for now to
                    // match GameWindow
                    g2d.drawImage(sprite, drawX, drawY, w, h, null);
                }
            }

            // 3. Draw Simple Text overlay if set
            if (currentText != null) {
                // Dim bottom
                int boxHeight = 150;
                int boxY = getHeight() - boxHeight - 20;

                g2d.setColor(new Color(0, 0, 0, 180));
                g2d.fillRoundRect(20, boxY, getWidth() - 40, boxHeight, 20, 20);

                g2d.setColor(Color.WHITE);
                if (currentSpeaker != null) {
                    g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
                    g2d.drawString(currentSpeaker, 40, boxY + 40);
                }

                g2d.setFont(new Font("SansSerif", Font.PLAIN, 18));
                // Very simple text wrapping would be needed here, keeping it single line/simple
                // for demo
                g2d.drawString(currentText, 40, boxY + 80);
            }
        }
    }
}
