package com.vnengine.game;

import com.vnengine.script.GameScript;

/**
 * ULTIMATE DEMO: Showcasing ALL features of the VN Engine!
 *
 * Features demonstrated:
 * - Scene backgrounds
 * - Character sprites with 10 expressions
 * - Character positioning, scaling, and animated movement
 * - Dialog system with narrator and character speech
 * - Choice menus with branching paths
 * - Window manipulation (shake, slide, resize, center, title)
 * - Dialog box positioning and animation
 * - Various easing functions
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

        narrator("Welcome to the Visual Novel Engine Demo!");
        narrator("This demo will showcase all the amazing features of this engine.");

        // ========================================
        // SCENE SETUP
        // ========================================
        scene("council");

        narrator("First, let's set the scene. This is the Student Council room.");
        narrator("Now, let me introduce you to someone special...");

        // ========================================
        // CHARACTER ENTRANCE - Dramatic Entry
        // ========================================

        // Show character off-screen first, then slide in
        show(SAKURA, "demo_girl_neutral", -400, 50, 0.8);
        move(SAKURA, 100, 50, 1000, "EASE_OUT_CUBIC");

        say(SAKURA, "Hello there! My name is Sakura, and I'll be your guide today!");

        // Expression change to happy
        show(SAKURA, "demo_girl_happy");

        say(SAKURA, "I'm SO excited to show you everything this engine can do!");

        // ========================================
        // EXPRESSION SHOWCASE
        // ========================================

        narrator("Let's start by showcasing the different expressions I can make!");

        show(SAKURA, "demo_girl_smile");
        say(SAKURA, "This is my gentle smile~");

        show(SAKURA, "demo_girl_smug");
        say(SAKURA, "Feeling a bit smug, aren't we? Hehe~");

        show(SAKURA, "demo_girl_surprised");
        windowShake(5, 200);
        say(SAKURA, "Oh! Did that startle you? That was a window shake!");

        show(SAKURA, "demo_girl_scared");
        say(SAKURA, "S-sometimes things can be a little scary...");

        show(SAKURA, "demo_girl_sad");
        say(SAKURA, "And sometimes I feel a bit down...");

        show(SAKURA, "demo_girl_angry");
        windowShake(10, 300);
        say(SAKURA, "But don't make me angry! HMPH!");

        show(SAKURA, "demo_girl_annoyed");
        say(SAKURA, "Okay okay, I'll calm down. Just a bit annoyed now.");

        show(SAKURA, "demo_girl_awkward");
        say(SAKURA, "Ahaha... that was a bit embarrassing...");

        show(SAKURA, "demo_girl_happy");
        say(SAKURA, "Alright! Back to being cheerful!");

        // ========================================
        // CHOICE MENU DEMO
        // ========================================

        narrator("Now let's try the choice system! Pick an option!");

        int choice1 = menu(
                "You're really cute, Sakura!",
                "Can you show me more features?",
                "Let's see some window tricks!",
                "Show me the Sub-Window Demo!");

        if (choice1 == 0) {
            flatteryPath();
        } else if (choice1 == 1) {
            featureShowcasePath();
        } else if (choice1 == 2) {
            windowTricksPath();
        } else {
            subWindowDemoPath();
        }

        // ========================================
        // FINAL SHOWCASE - Combined Features
        // ========================================
        show(SAKURA, "demo_girl_happy");
        move(SAKURA, 150, 50, 500);

        say(SAKURA, "Now for the grand finale! Watch this!");

        // Dramatic sequence
        narrator("Initiating Grand Finale sequence...");

        // Scale up dramatically
        show(SAKURA, "demo_girl_smug");
        scale(SAKURA, 1.2, 500, "EASE_OUT_ELASTIC");

        say(SAKURA, "Getting a bit closer~");

        // Window resize for dramatic effect
        resizeWindow(1200, 800, 800, "EASE_IN_OUT_CUBIC");

        say(SAKURA, "The window just got bigger! More room for drama!");

        // Shake it!
        windowShake(8, 400);
        show(SAKURA, "demo_girl_surprised");

        say(SAKURA, "Whoa! The ground is shaking!");

        // Slide the window around
        windowSlide(100, 100, 600, "EASE_IN_OUT_SINE");

        show(SAKURA, "demo_girl_happy");
        say(SAKURA, "And we're sliding around!");

        // Center it back
        windowCenter(800, "EASE_OUT_BOUNCE");

        // Reset size
        resizeWindow(960, 600, 600, "EASE_IN_OUT_CUBIC");

        // Reset scale
        scale(SAKURA, 0.8, 400);

        // Move to center
        move(SAKURA, 200, 50, 600, "EASE_OUT_CUBIC");

        show(SAKURA, "demo_girl_smile");

        say(SAKURA, "And one last thing...");
        show(SAKURA, "demo_girl_smug");
        say(SAKURA, "I'll show you a fake error!");
        fakeError("System Error", "Critical failure detected!");
        show(SAKURA, "demo_girl_awkward");
        say(SAKURA, "I know it's not that convicing, but hey! it worked!");
        show(SAKURA, "demo_girl_happy");
        say(SAKURA, "And you can only spawn one of them at a time");
        say(SAKURA, "If you spawn another one, the previous one will be removed");
        say(SAKURA, "Take a look if you don't believe me");
        fakeError("System Error", "Critical failure detected!");
        fakeError("System Error", "Critical failure detected!", 100, 200);
        fakeError("System Error", "Critical failure detected!", 200, 200);
        show(SAKURA, "demo_girl_sad");
        say(SAKURA, "See?");
        show(SAKURA, "demo_girl_neutral");
        say(SAKURA, "You can even take a look at the source code if you still don't believe me");
        say(SAKURA, "I mean you can alway edit the engine if you want");
        show(SAKURA, "demo_girl_smug");
        say(SAKURA, "Since this thing is open source anyway");
        show(SAKURA, "demo_girl_happy");

        // ========================================
        // CONCLUSION
        // ========================================

        say(SAKURA, "And that concludes our demo!");

        narrator("Features demonstrated in this demo:");
        narrator("✓ Multiple character expressions (10 in total!)");
        narrator("✓ Character positioning and animated movement");
        narrator("✓ Character scaling with animation");
        narrator("✓ Window shake effects");
        narrator("✓ Window sliding with easing");
        narrator("✓ Window resizing animation");
        narrator("✓ Window centering with bounce effect");
        narrator("✓ Interactive choice menus");
        narrator("✓ Branching storylines");
        narrator("✓ Narrator and character dialogue");
        narrator("✓ Fake error");

        show(SAKURA, "demo_girl_smile");

        say(SAKURA, "Thank you so much for watching the demo!");
        say(SAKURA, "I hope this engine helps you create amazing visual novels!");

        move(SAKURA, 180, 30, 800, "EASE_IN_OUT_CUBIC");
        scale(SAKURA, 0.9, 800);

        say(SAKURA, "Until next time... take care!");

        // Wave goodbye - slide off screen
        move(SAKURA, 1200, 30, 1000, "EASE_IN_CUBIC");

        narrator("~ THE END ~");
        narrator("Created with the Java Visual Novel Engine");

        // Stop the music
        stopMusic();

        windowTitle("Demo Complete!");
    }

    // ========================================
    // BRANCHING PATH: Flattery
    // ========================================
    private void flatteryPath() {
        show(SAKURA, "demo_girl_surprised");
        windowShake(3, 150);

        say(SAKURA, "Eh?! C-cute?!");

        show(SAKURA, "demo_girl_awkward");
        say(SAKURA, "I-I wasn't expecting that...");

        // Move closer, slightly bigger
        move(SAKURA, 200, 40, 600, "EASE_OUT_CUBIC");
        scale(SAKURA, 0.9, 600);

        show(SAKURA, "demo_girl_happy");
        say(SAKURA, "Hehe~ Thank you! You're too kind!");

        show(SAKURA, "demo_girl_smug");
        say(SAKURA, "Well, I DO have 10 different expressions, you know~");

        show(SAKURA, "demo_girl_smile");
        say(SAKURA, "Let me show off a bit more then!");

        // Quick expression reel with movement
        expressionReel();
    }

    // ========================================
    // BRANCHING PATH: Feature Showcase
    // ========================================
    private void featureShowcasePath() {
        show(SAKURA, "demo_girl_happy");

        say(SAKURA, "Of course! Let me show you some cool stuff!");

        // Dialog positioning
        narrator("First, let's play with the dialog box position!");

        dialogSlide(50, 100, 800, "EASE_OUT_ELASTIC");
        say(SAKURA, "The dialog box can move around the screen!");

        dialogSlide(300, 50, 600, "EASE_IN_OUT_CUBIC");
        say(SAKURA, "See? It's sliding smoothly!");

        dialogReset();
        say(SAKURA, "And back to the default position!");

        // Character movement demonstration
        narrator("Now let's see smooth character movement!");

        show(SAKURA, "demo_girl_smile");

        // Slide left
        move(SAKURA, -50, 50, 700, "EASE_OUT_CUBIC");
        say(SAKURA, "Moving to the left~");

        // Slide right
        move(SAKURA, 350, 50, 700, "EASE_OUT_CUBIC");
        say(SAKURA, "And to the right!");

        // Scale demonstration
        scale(SAKURA, 1.1, 500, "EASE_OUT_BOUNCE");
        show(SAKURA, "demo_girl_smug");
        say(SAKURA, "Getting a bit bigger~");

        scale(SAKURA, 0.6, 500, "EASE_IN_OUT_CUBIC");
        show(SAKURA, "demo_girl_scared");
        say(SAKURA, "Eek! Too small!");

        scale(SAKURA, 0.8, 400);
        move(SAKURA, 100, 50, 400);
        show(SAKURA, "demo_girl_happy");
        say(SAKURA, "Ahh, that's better!");
    }

    // ========================================
    // BRANCHING PATH: Window Tricks
    // ========================================
    private void windowTricksPath() {
        show(SAKURA, "demo_girl_smug");

        say(SAKURA, "Oh? You want to see some window magic?");
        say(SAKURA, "Hold onto your seat!");

        // Intense shake
        show(SAKURA, "demo_girl_surprised");
        windowShake(15, 500);
        say(SAKURA, "EARTHQUAKE!!!");

        show(SAKURA, "demo_girl_happy");
        say(SAKURA, "Just kidding~ That was the shake effect!");

        // Window slide
        narrator("The window can also slide around the screen!");

        windowSlide(0, 0, 800, "EASE_OUT_CUBIC");
        say(SAKURA, "Moving to the top-left corner!");

        windowSlide(500, 300, 600, "EASE_IN_OUT_SINE");
        say(SAKURA, "And somewhere else!");

        windowCenter(700, "EASE_OUT_BOUNCE");
        show(SAKURA, "demo_girl_smile");
        say(SAKURA, "Bouncing back to center with style!");

        // Resize demo
        narrator("The window can also resize dynamically!");

        resizeWindow(800, 500, 600, "EASE_IN_OUT_CUBIC");
        say(SAKURA, "Getting a bit smaller...");

        resizeWindow(1100, 700, 600, "EASE_OUT_ELASTIC");
        show(SAKURA, "demo_girl_surprised");
        say(SAKURA, "And now bigger! WOOSH!");

        resizeWindow(960, 600, 500);
        windowCenter(500);
        show(SAKURA, "demo_girl_happy");
        say(SAKURA, "Back to normal! Pretty cool, right?");
    }

    // ========================================
    // BRANCHING PATH: Sub-Window Demo
    // ========================================
    private void subWindowDemoPath() {
        scene("council");
        show(SAKURA, "demo_girl_smug");
        say(SAKURA, "Ooh, playing with fire? I like it!");

        narrator("PREPARE YOUR DESKTOP!");

        // Center main window first
        windowCenter();
        resizeWindow(854, 480, 1000, "EASE_OUT_CUBIC");
        windowSlide(675, 400, 1000, "EASE_OUT_CUBIC");
        scale(SAKURA, 0.85, 500, "EASE_OUT_CUBIC");
        windowTitle("Main Window - Control Center");

        say(SAKURA, "I'm going to spawn a clone of myself!");

        // Spawn Sub Window
        String subID = "clone_window";
        createSubWindow(subID, "Sakura's Clone", 640, 360);
        subWindowPos(subID, 0, 0); // Top left area
        subWindowBg(subID, "cafe_day"); // Different background

        showInSubWindow(subID, "Clone", "demo_girl_happy", 100, 0);
        move(SAKURA, 50, 25, 500);

        say(SAKURA, "Look! There she is!");

        // Main window interaction
        show(SAKURA, "demo_girl_surprised");
        say(SAKURA, "Hey you! Clone me!");

        // Sub window "talks"
        subWindowSay(subID, "Clone", "Hi! I'm the sub-window version!");

        // Pause to let user read
        say(SAKURA, "She can talk too! This is so cool!");

        subWindowSay(subID, "Clone", "I can even change expressions independently!");
        showInSubWindow(subID, "Clone", "demo_girl_smug", 100, 0);

        say(SAKURA, "Wait, why are you making that face?");

        subWindowSay(subID, "Clone", "Because I exist outside your main loop!");

        // Move Sub Window
        windowSlide(1000, 400, 1000, "EASE_OUT_CUBIC");
        for (int i = 0; i < 5; i++) {
            int x = 100 + i * 50;
            int y = 100 + i * 50;
            subWindowPos(subID, x, y);
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        }

        say(SAKURA, "Hey, where are you going?");
        subWindowSay(subID, "Clone", "I'm sliding away~");

        say(SAKURA, "Come back!");

        // Close it
        say(SAKURA, "Okay, that's enough confusion for one day.");
        closeSubWindow(subID);

        show(SAKURA, "demo_girl_happy");
        windowTitle("VN Engine - Demo Showcase");
        resizeWindowCentered(1280, 720, 1000, "EASE_OUT_CUBIC");
        say(SAKURA, "And she's gone! But you can spawn as many as you want!");
    }

    // ========================================
    // HELPER: Quick Expression Reel
    // ========================================
    private void expressionReel() {
        narrator("Watch this quick expression reel!");

        String[] expressions = {
                "demo_girl_neutral",
                "demo_girl_happy",
                "demo_girl_smile",
                "demo_girl_smug",
                "demo_girl_surprised",
                "demo_girl_awkward",
                "demo_girl_sad",
                "demo_girl_angry",
                "demo_girl_annoyed",
                "demo_girl_scared"
        };

        String[] names = {
                "Neutral",
                "Happy",
                "Smile",
                "Smug",
                "Surprised",
                "Awkward",
                "Sad",
                "Angry",
                "Annoyed",
                "Scared"
        };

        for (int i = 0; i < expressions.length; i++) {
            show(SAKURA, expressions[i]);
            say(SAKURA, names[i] + "!");
        }

        show(SAKURA, "demo_girl_happy");
        say(SAKURA, "And that's all 10 expressions!");
    }

    public static void main(String[] args) {
        com.vnengine.Main.main(args);
    }
}
