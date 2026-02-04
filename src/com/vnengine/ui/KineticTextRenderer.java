package com.vnengine.ui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KineticTextRenderer {
    private String rawText;
    private List<Glyph> glyphs = new ArrayList<>();
    private float visibleGlyphs = 0;
    private float typeSpeed = 0.5f; // Glyphs per frame (at 60fps) -> 30 chars/sec

    public void setTypeSpeed(float speed) {
        this.typeSpeed = speed;
    }

    private boolean isFinished = false;
    private long lastTime;

    // Animation states
    private float time = 0; // Accumulated time for wave effects

    private Font baseFont;
    private Color defaultColor = Color.WHITE;

    public KineticTextRenderer() {
        baseFont = new Font("SansSerif", Font.PLAIN, 24);
    }

    public void setText(String text) {
        this.rawText = text;
        this.glyphs.clear();
        this.visibleGlyphs = 0;
        this.isFinished = false;
        this.time = 0;
        parseText(text);
    }

    public void setFont(Font font) {
        this.baseFont = font;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void skip() {
        visibleGlyphs = glyphs.size();
        isFinished = true;
    }

    public void update() {
        if (!isFinished) {
            visibleGlyphs += typeSpeed;
            if (visibleGlyphs >= glyphs.size()) {
                visibleGlyphs = glyphs.size();
                isFinished = true;
            }
        }
        time += 0.1f;
    }

    public void draw(Graphics2D g, int x, int y, int maxWidth) {
        g.setFont(baseFont);
        FontMetrics fm = g.getFontMetrics();
        int lineHeight = fm.getHeight();

        int currentX = x;
        int currentY = y;

        int limit = (int) visibleGlyphs;

        int spaceWidth = fm.charWidth(' ');

        for (int i = 0; i < glyphs.size(); i++) {
            Glyph glyph = glyphs.get(i);

            // Check if we need to wrap *this word*
            // Find end of current word
            if (glyph.c != ' ' && (i == 0 || glyphs.get(i - 1).c == ' ')) {
                int wordWidth = 0;
                for (int j = i; j < glyphs.size(); j++) {
                    Glyph g2 = glyphs.get(j);
                    if (g2.c == ' ')
                        break;
                    wordWidth += fm.charWidth(g2.c);
                }
                if (currentX + wordWidth > x + maxWidth) {
                    currentX = x;
                    currentY += lineHeight;
                }
            }

            // Apply effects to position
            int drawX = currentX;
            int drawY = currentY;

            if (glyph.shake) {
                drawX += (Math.random() - 0.5) * 4; // +/- 2px
                drawY += (Math.random() - 0.5) * 4;
            }
            if (glyph.wave) {
                drawY += Math.sin(time + (i * 0.5)) * 5;
            }

            // Draw
            if (i < limit) {
                g.setColor(glyph.color != null ? glyph.color : defaultColor);
                g.drawString(String.valueOf(glyph.c), drawX, drawY);
            }

            currentX += fm.charWidth(glyph.c);
        }
    }

    private void parseText(String text) {

        String pattern = "(\\[/?[a-z]+(=#?[a-fA-F0-9]+)?\\])";
        String[] parts = text.split(pattern);

        // To reconstruct correctly, we need the matches too.
        Matcher m = Pattern.compile(pattern).matcher(text);

        int lastIdx = 0;

        boolean shake = false;
        boolean wave = false;
        Color currentColor = null;

        while (m.find()) {
            String segment = text.substring(lastIdx, m.start());
            addSegment(segment, shake, wave, currentColor);

            String tag = m.group();
            // Process tag
            if (tag.equals("[shake]"))
                shake = true;
            else if (tag.equals("[/shake]"))
                shake = false;
            else if (tag.equals("[wave]"))
                wave = true;
            else if (tag.equals("[/wave]"))
                wave = false;
            else if (tag.startsWith("[color=")) {
                String val = tag.substring(7, tag.length() - 1);
                try {
                    if (val.startsWith("#"))
                        currentColor = Color.decode(val);
                    else if (val.equalsIgnoreCase("red"))
                        currentColor = Color.RED;
                    else if (val.equalsIgnoreCase("blue"))
                        currentColor = Color.BLUE;
                    else if (val.equalsIgnoreCase("green"))
                        currentColor = Color.GREEN;
                    // add more as needed
                } catch (Exception e) {
                }
            } else if (tag.equals("[/color]"))
                currentColor = null;

            lastIdx = m.end();
        }

        if (lastIdx < text.length()) {
            addSegment(text.substring(lastIdx), shake, wave, currentColor);
        }
    }

    private void addSegment(String text, boolean shake, boolean wave, Color color) {
        for (char c : text.toCharArray()) {
            Glyph g = new Glyph();
            g.c = c;
            g.shake = shake;
            g.wave = wave;
            g.color = color;
            glyphs.add(g);
        }
    }

    private static class Glyph {
        char c;
        boolean shake;
        boolean wave;
        Color color;
    }
}
