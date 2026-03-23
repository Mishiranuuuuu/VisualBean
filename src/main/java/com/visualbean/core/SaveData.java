package com.visualbean.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SaveData implements Serializable {
    private static final long serialVersionUID = 2L;

    public int stepIndex;
    public String timestamp;
    public String snapshotText;

    public String currentBackground;
    public String currentMusic;
    public java.util.Map<String, String> visibleCharacters;
    public java.util.Map<String, java.awt.Point> characterPositions;
    public java.util.Map<String, Double> characterScales;
    public java.awt.Dimension windowSize;
    public java.awt.Point windowPosition;
    public String windowTitle;
    public java.awt.Point dialogPosition;

    // Tracks which choices were made so branches can be replayed correctly on load
    public List<Integer> choiceHistory = new ArrayList<>();

    public String description;

    public SaveData(int stepIndex, String text) {
        this.stepIndex = stepIndex;
        this.timestamp = new Date().toString();
        this.snapshotText = text;
    }
}
