package com.vnengine.ui;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * StyleManager - CSS-like styling system for the Visual Novel Engine
 * 
 * Supports the following selectors:
 * - .dialog-box : The main dialogue box
 * - .menu-button : Choice menu buttons
 * - .toolbar-button : History/Save/Load toolbar buttons
 * - .overlay-panel : The modal overlay panel (history/save/load)
 * - .overlay-title : Title text in overlay panels
 * - .overlay-close : Close button on overlay panels
 * - .history-entry : Individual history log entries
 * - .history-speaker : Speaker name in history
 * - .history-text : Dialogue text in history
 * - .save-slot : Save/Load slot boxes
 * - .save-slot-empty : Empty save slots
 * - .save-slot-filled : Filled save slots
 * - .scrollbar : Scrollbar styling
 * - .hint-text : Hint text at bottom of overlays
 */
public class StyleManager {
    private static StyleManager instance;
    private Map<String, Map<String, String>> styles;
    private long lastModified = 0;
    private File themeFile;

    private StyleManager() {
        styles = new HashMap<>();
        themeFile = new File("resources/theme.css");
        loadTheme();
    }

    public static StyleManager getInstance() {
        if (instance == null) {
            instance = new StyleManager();
        }
        return instance;
    }

    /**
     * Reload theme if the file has been modified
     */
    public void checkReload() {
        if (themeFile.exists() && themeFile.lastModified() > lastModified) {
            loadTheme();
        }
    }

    public void loadTheme() {
        if (!themeFile.exists())
            return;
        lastModified = themeFile.lastModified();
        styles.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(themeFile))) {
            String line;
            String currentSelector = null;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("/*") || line.startsWith("//"))
                    continue;

                if (line.endsWith("{")) {
                    currentSelector = line.substring(0, line.length() - 1).trim();
                    styles.putIfAbsent(currentSelector, new HashMap<>());
                } else if (line.equals("}")) {
                    currentSelector = null;
                } else if (currentSelector != null && line.contains(":")) {
                    int colonIdx = line.indexOf(":");
                    String key = line.substring(0, colonIdx).trim();
                    String value = line.substring(colonIdx + 1).replace(";", "").trim();
                    styles.get(currentSelector).put(key, value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Color getColor(String selector, String property, Color defaultColor) {
        String val = getProperty(selector, property);
        if (val == null)
            return defaultColor;
        try {
            // Support rgba format: rgba(r, g, b, a)
            if (val.startsWith("rgba(")) {
                String inner = val.substring(5, val.length() - 1);
                String[] parts = inner.split(",");
                int r = Integer.parseInt(parts[0].trim());
                int g = Integer.parseInt(parts[1].trim());
                int b = Integer.parseInt(parts[2].trim());
                int a = Integer.parseInt(parts[3].trim());
                return new Color(r, g, b, a);
            }
            // Support rgb format: rgb(r, g, b)
            if (val.startsWith("rgb(")) {
                String inner = val.substring(4, val.length() - 1);
                String[] parts = inner.split(",");
                int r = Integer.parseInt(parts[0].trim());
                int g = Integer.parseInt(parts[1].trim());
                int b = Integer.parseInt(parts[2].trim());
                return new Color(r, g, b);
            }
            // Hex format
            return Color.decode(val);
        } catch (Exception e) {
            return defaultColor;
        }
    }

    public int getInt(String selector, String property, int defaultValue) {
        String val = getProperty(selector, property);
        if (val == null)
            return defaultValue;
        try {
            // Remove 'px' suffix if present
            val = val.replace("px", "").trim();
            return Integer.parseInt(val);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public float getFloat(String selector, String property, float defaultValue) {
        String val = getProperty(selector, property);
        if (val == null)
            return defaultValue;
        try {
            return Float.parseFloat(val);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public String getString(String selector, String property, String defaultValue) {
        String val = getProperty(selector, property);
        return val != null ? val : defaultValue;
    }

    public Font getFont(String selector, int defaultStyle, int defaultSize) {
        String family = getString(selector, "font-family", "SansSerif");
        int size = getInt(selector, "font-size", defaultSize);
        String weight = getString(selector, "font-weight", "normal");
        String style = getString(selector, "font-style", "normal");

        int fontStyle = Font.PLAIN;
        if (weight.equals("bold"))
            fontStyle |= Font.BOLD;
        if (style.equals("italic"))
            fontStyle |= Font.ITALIC;

        // Use default style if none specified in CSS
        if (fontStyle == Font.PLAIN && defaultStyle != Font.PLAIN) {
            fontStyle = defaultStyle;
        }

        return new Font(family, fontStyle, size);
    }

    public boolean getBoolean(String selector, String property, boolean defaultValue) {
        String val = getProperty(selector, property);
        if (val == null)
            return defaultValue;
        return val.equalsIgnoreCase("true") || val.equals("1");
    }

    private String getProperty(String selector, String property) {
        if (styles.containsKey(selector)) {
            return styles.get(selector).get(property);
        }
        return null;
    }

    /**
     * Check if a selector exists in the loaded styles
     */
    public boolean hasSelector(String selector) {
        return styles.containsKey(selector);
    }
}
