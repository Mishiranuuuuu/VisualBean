package com.visualbean;

import com.visualbean.core.GameEngine;
import com.visualbean.game.MyGame;
import com.visualbean.script.GameScript;
import com.visualbean.util.CrashLogger;

public class Main {
    public static void main(String[] args) {
        CrashLogger.installGlobalHandler();

        try {
            GameEngine engine = new GameEngine();
            engine.start();

            engine.showMainMenu(() -> {
                GameScript script = new MyGame();
                script.setEngine(engine);
                engine.executeScript(script);
            });
        } catch (Exception e) {
            CrashLogger.log("Main entry point", e);
            throw e;
        }
    }
}
