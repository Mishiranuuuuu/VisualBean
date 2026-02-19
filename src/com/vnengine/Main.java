package com.vnengine;

import com.vnengine.core.GameEngine;
import com.vnengine.game.MyGame;
import com.vnengine.script.GameScript;

public class Main {
    public static void main(String[] args) {
        GameEngine engine = new GameEngine();
        engine.start();

        engine.showMainMenu(() -> {
            GameScript script = new MyGame();
            script.setEngine(engine);
            engine.executeScript(script);
        });
    }
}
