# Java Visual Novel Engine - User Manual

Welcome to the **Java Visual Novel Engine**! This manual documents how to create your own visual novels, covering everything from basic script writing to advanced feature like window manipulation and sub-windows.

## 1. Getting Started

### Project Structure
*   **`src/com/vnengine/game/`**: Your game scripts (Java files) live here.
*   **`resources/`**: Your assets repository.
    *   `backgrounds/`: Background images (JPG/PNG).
    *   `characters/`: Character sprites (PNG with transparency).
    *   `audio/`: Music and Sound Effects (WAV/AU).
*   **`resources/theme.css`**: UI styling.

### Creating Your First Script
1.  Create a new file `MyStory.java` in `src/com/vnengine/game/`.
2.  Extend `GameScript` and implement `run()`.

```java
package com.vnengine.game;

import com.vnengine.script.GameScript;

public class MyStory extends GameScript {
    @Override
    public void run() {
        scene("classroom");
        playMusic("happy_vibes");
        
        show("Alice", "alice_smile", 100, 200);
        say("Alice", "Hello! Welcome to my visual novel.");
        
        narrator("This is a simple narration line.");
    }
}
```

3.  **Activate it**: Open `src/com/vnengine/Main.java`, find `engine.executeScript(...)` and change `new MyGame()` to `new MyStory()`.

---

## 2. API Reference

All methods below are available inside your `GameScript` class.

### Narrative Control
*   **`say(name, text)`**: Character speaks.
*   **`narrator(text)`**: Narration text (no name).
*   **`menu(option1, option2, ...)`**: Displays a choice menu. Returns the index selected (0, 1, 2...).

```java
int choice = menu("Do you like cats?", "Yes", "No");
if (choice == 0) {
    say("Alice", "Me too!");
} else {
    say("Alice", "Oh, I see...");
}
```

### Visuals & Characters
*   **`scene(imageName)`**: Sets the background (file name in `resources/backgrounds` without extension).
*   **`show(name, imageName, [x], [y], [scale])`**: Displays a character. 
    *   `x, y`: Screen coordinates (top-left is 0,0).
    *   `scale`: Size multiplier (1.0 is default).
*   **`hide(name)`**: Removes a character.
*   **`move(name, x, y, duration, [easing])`**: Animates character movement.
*   **`scale(name, factor, duration, [easing])`**: Animates character scaling.

```java
// Show Alice at (100, 200)
show("Alice", "alice_neutral", 100, 200);

// Slide her to (500, 200) over 1 second with a bounce effect
move("Alice", 500, 200, 1000, "EASE_OUT_BOUNCE");

// Scale her up to 1.5x over 0.5 seconds
scale("Alice", 1.5, 500, "EASE_IN_OUT_CUBIC");
```

### Audio
*   **`playMusic(name)`** / **`music(name)`**: Loops a music track from `resources/audio`.
*   **`stopMusic()`**: Stops currently playing music.
*   **`playSound(name)`** / **`sound(name)`**: Plays a sound effect once.

### Window & UI Manipulation (Meta Features)
Break the fourth wall or create dynamic effects by manipulating the game window itself.

*   **`windowTitle(text)`**: Changes the OS window title.
*   **`windowShake(intensity, duration)`**: Shakes the application window.
*   **`windowMove(x, y)`**: Instantly moves the window on screen.
*   **`windowSlide(x, y, duration, [easing])`**: Smoothly slides the window across the screen.
*   **`windowCenter([duration], [easing])`**: Centers the window on the monitor.
*   **`resizeWindow(w, h, duration, [easing])`**: Smoothly resizes the window.
*   **`resizeWindowCentered(w, h, duration, [easing])`**: Resizes while changing position to stay centered.
*   **`dialogPos(x, y)`**: Sets the dialogue box position.
*   **`dialogSlide(x, y, duration, [easing])`**: Animates the dialogue box.
*   **`fakeError(title, message, [x, y])`**: Spawns a fake system error popup. Only one exists at a time.

```java
windowShake(10, 500); // Shake intensity 10 for 0.5s
fakeError("System Failure", "Just kidding!");
```

### Sub-Windows (Multi-Window Support)
Create secondary floating windows that contain their own backgrounds and characters.

*   **`createSubWindow(id, title, w, h)`**: Spawns a new window.
*   **`subWindowBg(id, imageName)`**: Sets background for sub-window.
*   **`showInSubWindow(id, name, image, x, y)`**: Shows character in sub-window.
*   **`subWindowSay(id, name, text)`**: Displays text in the sub-window (does not halt main script).
*   **`moveInSubWindow(id, name, x, y)`**: Moves character within sub-window.
*   **`subWindowPos(id, x, y)`**: Moves the sub-window itself on screen.
*   **`closeSubWindow(id)`**: Destroys the sub-window.

---

## 3. Animations & Easing

For any method taking a `duration` (in milliseconds) and `easing` (String), you can control the "feel" of the animation.

**Common Easing Functions:**
*   `"LINEAR"`: Constant speed.
*   `"EASE_IN_QUAD"`, `"EASE_OUT_QUAD"`: Smooth start or end.
*   `"EASE_IN_OUT_CUBIC"`: Very smooth acceleration and deceleration (Standard).
*   `"EASE_OUT_BOUNCE"`: Bounces at the end (Great for character entry or window drop).
*   `"EASE_OUT_ELASTIC"`: Wiggles like jelly.

---

## 4. Customizing Themes (`theme.css`)

Edit `resources/theme.css` to completely change the look of the interface. You can verify changes by restarting the game.

**Key Classes:**
*   `.dialog-box`: The main text area.
*   `.name-label`: The character name box.
*   `.menu-button`: Choice buttons.
*   `.save-slot`: Save/Load entries.

---

## 5. Building for Release

1.  Run **`build_game.bat`**.
2.  Wait for the compilation to finish.
3.  Check the **`dist/`** folder.
4.  Zip the **`dist`** folder and share it!
