package com.visualbean;

import com.visualbean.core.GameEngine;
import com.visualbean.game.MyGame;
import com.visualbean.script.GameScript;

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
