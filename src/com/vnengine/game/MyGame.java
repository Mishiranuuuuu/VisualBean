package com.vnengine.game;

import com.vnengine.script.GameScript;

/**
 * ULTIMATE DEMO: Showcasing ALL features of the VN Engine!
 */
public class MyGame extends GameScript {

    // Character name constant
    private static final String SAKURA = "Sakura";

    @Override
    public void run() {
        // ========================================
        // INTRO - Window and Title Setup
        // ========================================
        windowTitle("VN Engine - Demo Showcase");

        // Start the background music
        playMusic("Beautiful Day");

        // Start with a dramatic window center
        windowCenter(800, "EASE_OUT_BOUNCE");

        narrator("Welcome to the Ultimate Visual Novel Engine Demo!");
        narrator("This demo will showcase all the amazing features available to you.");
        narrator("From character animations to multi-window tricks, we have it all!");

        // ========================================
        // SCENE SETUP
        // ========================================
        scene("council");

        // Show character off-screen first, then slide in
        show(SAKURA, "demo_girl_neutral", -400, 50, 0.8);
        move(SAKURA, 100, 50, 1000, "EASE_OUT_CUBIC");

        say(SAKURA, "Hello there! My name is Sakura, and I'll be your guide today!");

        // Expression change to happy
        show(SAKURA, "demo_girl_happy");
        say(SAKURA, "I'm SO excited to show you everything this engine can do!");

        // ========================================
        // MAIN MENU LOOP
        // ========================================
        boolean running = true;
        while (running) {
            show(SAKURA, "demo_girl_smile");
            say(SAKURA, "What would you like to see?");

            int choice = menu(
                    "Character Features (Expressions, Movement, Scale)",
                    "Window & UI Effects (Shake, Slide, Dialog)",
                    "Sub-Window Magic (Multi-window demo)",
                    "System Features (Save/Load, Errors, Website)",
                    "End Demo");

            if (choice == 0) {
                characterDemo();
            } else if (choice == 1) {
                windowDemo();
            } else if (choice == 2) {
                subWindowDemo();
            } else if (choice == 3) {
                systemDemo();
            } else {
                running = false;
            }
        }

        conclusion();
    }

    // ========================================
    // DEMO: Characters
    // ========================================
    private void characterDemo() {
        show(SAKURA, "demo_girl_smug");
        say(SAKURA, "Let's start with the basics: Expressions and Movement!");

        // Expressions
        narrator("Characters can have many expressions.");

        String[] expressions = {
                "demo_girl_smile", "demo_girl_surprised", "demo_girl_sad",
                "demo_girl_angry", "demo_girl_awkward", "demo_girl_happy"
        };

        for (String expr : expressions) {
            show(SAKURA, expr);
            try {
                Thread.sleep(500);
            } catch (Exception e) {
            }
        }

        say(SAKURA, "And that's just a few of them!");

        // Movement
        narrator("We can also move smoothly across the screen.");
        show(SAKURA, "demo_girl_happy");

        move(SAKURA, 400, 50, 800, "EASE_IN_OUT_CUBIC");
        say(SAKURA, "Sliding to the right!");

        move(SAKURA, 100, 50, 800, "EASE_OUT_BOUNCE");
        say(SAKURA, "Bouncing back to the left!");

        // Scaling
        narrator("And scaling!");
        scale(SAKURA, 1.2, 500, "EASE_OUT_ELASTIC");
        show(SAKURA, "demo_girl_surprised");
        say(SAKURA, "Whoa! Too close!");

        scale(SAKURA, 0.8, 500);
        show(SAKURA, "demo_girl_smile");
        say(SAKURA, "That's better.");
    }

    // ========================================
    // DEMO: Window & UI
    // ========================================
    private void windowDemo() {
        show(SAKURA, "demo_girl_smug");
        say(SAKURA, "Now for the fun meta-magic!");

        // Shake
        windowShake(10, 500);
        show(SAKURA, "demo_girl_surprised");
        say(SAKURA, "Earthquake! (That was a window shake)");

        // Window Move
        say(SAKURA, "I can move the actual game window!");
        windowSlide(100, 100, 800, "EASE_IN_OUT_CUBIC");
        say(SAKURA, "Up here now!");

        windowCenter(800, "EASE_OUT_BOUNCE");
        say(SAKURA, "And back to center.");

        // Resize
        say(SAKURA, "I can even change the window size dynamically.");
        resizeWindow(1280, 720, 800, "EASE_OUT_ELASTIC");
        say(SAKURA, "Widescreen!");
        resizeWindow(960, 600, 800, "EASE_IN_OUT_CUBIC");

        // Dialog Box Animation
        narrator("The dialog box can also be animated.");
        dialogSlide(100, 100, 800, "EASE_OUT_CUBIC");
        say(SAKURA, "See? I'm floating up here!");
        dialogReset();
        say(SAKURA, "Back to normal.");
    }

    // ========================================
    // DEMO: Sub-Windows
    // ========================================
    private void subWindowDemo() {
        show(SAKURA, "demo_girl_happy");
        say(SAKURA, "This is my favorite feature: Multi-Window support!");

        // Create Sub Window
        String subID = "clone_win";
        createSubWindow(subID, "Sakura's Clone", 640, 360);
        subWindowPos(subID, 100, 100);
        subWindowBg(subID, "cafe_day");

        showInSubWindow(subID, "Clone", "demo_girl_happy", 100, 0);

        say(SAKURA, "I've created a clone in another window!");

        subWindowSay(subID, "Clone", "Hi! I'm running in a separate window!");
        say(SAKURA, "She can talk, move, and emote independently.");

        // Move character in subwindow
        moveInSubWindow(subID, "Clone", 300, 0);
        subWindowSay(subID, "Clone", "I'm moving inside my window!");

        showInSubWindow(subID, "Clone", "demo_girl_smug", 300, 0);
        say(SAKURA, "You can control as many of these as your PC can handle!");

        // Close
        say(SAKURA, "Bye bye clone!");
        closeSubWindow(subID);
        say(SAKURA, "And she's gone.");
    }

    // ========================================
    // DEMO: System Features
    // ========================================
    private void systemDemo() {
        show(SAKURA, "demo_girl_smile");
        say(SAKURA, "There are also powerful system features.");

        // Native Error
        say(SAKURA, "For example, native system error popups.");
        fakeError("System Warning", "This is a native error popup triggered by the script!");
        say(SAKURA, "Did you see that popup? Useful for fourth-wall breaks!");

        // Save/Load
        narrator("The engine supports a full Save/Load system.");
        narrator("You can save by pausing or using the menu features.");
        narrator("You can also delete saves if you make a mistake.");

        // Website
        int webChoice = menu("Do you want to visit the engine website?", "Yes, open it!", "Maybe later.");
        if (webChoice == 0) {
            website("https://google.com");
            say(SAKURA, "Opened it for you!");
        } else {
            say(SAKURA, "No problem!");
        }

        // Fullscreen
        say(SAKURA, "Oh, and we support Fullscreen too!");
        /*
         * Note: Switching to fullscreen might change resolution,
         * so we usually let the user do it via Settings, but we can force it.
         * Uncomment below to test:
         * windowFullscreen(true);
         * say(SAKURA, "Now we are fullscreen!");
         * windowFullscreen(false);
         * say(SAKURA, "And back to windowed.");
         */
    }

    // ========================================
    // CONCLUSION
    // ========================================
    private void conclusion() {
        show(SAKURA, "demo_girl_happy");
        say(SAKURA, "And that concludes our demo!");

        say(SAKURA, "Thank you for checking out the Java Visual Novel Engine.");
        say(SAKURA, "Have fun creating your own stories!");

        move(SAKURA, 1200, 50, 1000, "EASE_IN_CUBIC");
        narrator("~ THE END ~");

        stopMusic();
        windowTitle("Demo Complete!");
    }

    public static void main(String[] args) {
        com.vnengine.Main.main(args);
    }
}
