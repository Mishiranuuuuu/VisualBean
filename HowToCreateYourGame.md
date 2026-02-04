# How to Create Your Own Visual Novel

Welcome to the **Java Visual Novel Engine**! This engine is designed to be a flexible and easy-to-use framework for creating your own visual novels using Java and CSS.

## 1. Project Structure

Here is a quick overview of the most important folders for you:

*   **`src/com/vnengine/game/`**: This is where your game scripts live.
*   **`resources/`**: This is where all your assets (images, audio, theme) go.
    *   `resources/backgrounds/`: Background images.
    *   `resources/characters/`: Character sprites.
    *   `resources/audio/`: Music and sound effects.
    *   `resources/theme.css`: The styling file for the UI.

## 2. Writing Your Story

The core of your game is a **Script**. The engine comes with an example script in `src/com/vnengine/game/MyGame.java`.

### Creating a New Script
1.  Create a new Java class in `src/com/vnengine/game/` (e.g., `MyNewStory.java`).
2.  Make it extend `GameScript`.
3.  Implement the `run()` method.

```java
package com.vnengine.game;

import com.vnengine.script.GameScript;

public class MyNewStory extends GameScript {
    @Override
    public void run() {
        // Setup the scene
        scene("classroom_bg"); 
        windowTitle("My First Visual Novel");

        // Character enters
        show("Alice", "alice_happy", 100, 150);
        
        // Dialogue
        narrator("It was a bright sunny day.");
        say("Alice", "Hello there! Welcome to my game.");
        
        // Choice
        int choice = menu("What should I say?", 
            "Hello Alice!", 
            "I'm busy."
        );
        
        if (choice == 0) {
            say("Me", "Hello Alice! Nice to meet you.");
            show("Alice", "alice_blush");
            say("Alice", "Oh, you're so polite!");
        } else {
            say("Me", "I'm busy right now.");
            show("Alice", "alice_sad");
            say("Alice", "Oh... okay then.");
        }
    }
}
```

### Key Commands
*   **`scene(imageName)`**: Sets the background image.
*   **`show(name, imageName, x, y)`**: Shows a character sprite.
*   **`say(name, text)`**: distinct dialogue line. Use `narrator(text)` for narration.
*   **`menu(prompt, option1, option2...)`**: Shows a choice menu. Returns the index of the selected option (0, 1, 2...).
*   **`playMusic(name)`** / **`playSound(name)`**: Plays audio.

## 3. Customizing the Look (Theming)

You don't need to touch Java code to change the UI colors! Open **`resources/theme.css`**.

You can customize:
*   **Main Menu**: Colors, gradients, title font.
*   **Dialogue Box**: Background opacity, text colors, fonts, padding.
*   **Buttons**: Colors, hover effects, border radius.
*   **Save/Load Screens**: Colors for empty/filled slots.

**Example `theme.css` snippet:**
```css
.dialog-box {
    background-color: #000000;
    opacity: 180; /* Make it semi-transparent */
    border-color: #FFFFFF;
    text-color: #FFFFFF;
    font-family: SansSerif;
    font-size: 20;
}
```

## 4. Setting Up Your Game

To tell the engine to run *your* script instead of the default one:

1.  Open `src/com/vnengine/Main.java`.
2.  Find the line `GameScript script = new MyGame();`.
3.  Change `MyGame` to the name of your class (e.g., `MyNewStory`).

```java
// Inside Main.java
engine.showMainMenu(() -> {
    GameScript script = new MyNewStory(); // <-- Change this line
    script.setEngine(engine);
    engine.executeScript(script);
});
```

## 5. Adding Assets

*   **Images**: .png, .jpg supported. Place in `resources/backgrounds` or `resources/characters`.
*   **Audio**: .wav, .au supported (standard Java support). Place in `resources/audio`.

## 6. Building for Release

To turn your project into a playable game that you can share:

1.  Run the **`build_game.bat`** script found in the main project folder.
2.  Wait for the process to complete.
3.  A new folder named **`dist`** will be created.
4.  Inside `dist`, you will find:
    *   `Game.jar`: The game executable.
    *   `resources/`: Your assets.
    *   `Play.bat`: A launcher script.
5.  **To Share**: simply Zip the entire `dist` folder and send it to your players!

Happy Creating!
