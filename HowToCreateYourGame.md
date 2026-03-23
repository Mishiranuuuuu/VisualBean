# VisualBean Engine - User Manual

Welcome to **VisualBean**! This manual documents how to create your own visual novels, covering everything from basic script writing to advanced features like window manipulation, dynamic scaling, and sub-windows.

## 1. Getting Started

### Prerequisites

*   **Java Development Kit (JDK) 17** or higher installed.
*   **Gradle** is included via the Gradle wrapper (`gradlew` / `gradlew.bat`), so no separate installation is needed.

### Project Structure
*   **`src/main/java/com/visualbean/game/`**: Your game scripts (Java files) live here.
*   **`src/main/resources/`**: Your assets repository (Gradle standard layout).
    *   `backgrounds/`: Background images (JPG/PNG).
    *   `characters/`: Character sprites (PNG with transparency).
    *   `audio/`: Music and Sound Effects (WAV/AU).
*   **`resources/theme.css`**: UI styling.
*   **`build.gradle`**: Gradle build configuration.

### Creating Your First Script
1.  Go to `MyGame.java` in `src/main/java/com/visualbean/game/`.
2.  Extend `GameScript` and implement `run()`.

```java
package com.visualbean.game;

import com.visualbean.script.GameScript;

public class MyGame extends GameScript {
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

### Running Your Game

```bash
# Windows
gradlew.bat run

# Linux / macOS
./gradlew run
```

---

## 2. API Reference

All methods below are available inside your `GameScript` class.

### Narrative Control
*   **`say(name, text)`**: Character speaks.
*   **`narrator(text)`**: Narration text (no name).
*   **`menu(option1, option2, ...)`**: Displays a choice menu. Returns the index selected (0, 1, 2...).

```java
say("Alice", "Do you like cats?");
int choice = menu("Yes", "No");
if (choice == 0) {
    say("Alice", "Me too!");
} else {
    say("Alice", "Oh, I see...");
}
```
You can use switch case and create branch path with

```java
say("Alice", "Do you like cats?");
int choice = menu("Yes", "No");
switch(choice) {
   case 0 -> LikeCatYes();
   case 1 -> LikeCateNo();
}
```

### Kinetic Text Tags

You can embed animated text effects directly in dialogue strings:

*   **`[shake]text[/shake]`**: Shaking text effect.
*   **`[wave]text[/wave]`**: Waving text effect.
*   **`[color=#RRGGBB]text[/color]`**: Colored text.

These tags can be combined freely:

```java
say("Alice", "This is [shake]shaking[/shake], this is [wave]waving[/wave], and this is [color=#ff0000]red text[/color]!");
```

### Visuals & Characters
*   **`scene(imageName)`**: Sets the background (file name in `resources/backgrounds` without extension).
*   **`show(name, imageName, [x], [y], [scale])`**: Displays a character. 
    *   `x, y`: Screen coordinates (top-left is 0,0).
    *   `scale`: Size multiplier (1.0 is default).
*   **`hide(name)`**: Removes a character.
*   **`move(name, x, y, [duration], [easing])`**: Moves a character (instantly or animated).
*   **`scale(name, factor, [duration], [easing])`**: Scales a character (instantly or animated).

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
*   **`windowSize(width, height)`**: Instantly sets the window size.
*   **`windowFullscreen(boolean)`**: Toggles fullscreen mode (`true` to enter, `false` to exit).
*   **`resizeWindow(w, h, duration, [easing])`**: Smoothly resizes the window.
*   **`resizeWindowCentered(w, h, duration, [easing])`**: Resizes while staying centered.
*   **`dialogPos(x, y)`**: Sets the dialogue box position.
*   **`dialogSlide(x, y, duration, [easing])`**: Animates the dialogue box.
*   **`dialogReset()`**: Resets the dialogue box to its default position.
*   **`fakeError(title, message, [x, y])`**: Spawns a system error popup.
*   **`clearFakeError()`**: Removes any active fake error popup.
*   **`changeWallpaper(imagePath)`**: Changes the user's desktop wallpaper (Windows only).
*   **`restoreWallpaper()`**: Restores the user's original desktop wallpaper.
*   **`getWallpaper()`**: Returns the file path of the current desktop wallpaper.
*   **`website(url)`** / **`openWeb(url)`**: Opens the specified URL in the default web browser.
*   **`notification(title, message)`** / **`notify(title, message)`**: Sends a Windows system notification.
*   **`shutDown()`**: Force shuts down the user's computer (auto-saves before shutdown).

```java
windowShake(10, 500); // Shake intensity 10 for 0.5s
windowFullscreen(true); // Enter fullscreen
fakeError("System Failure", "This is a real popup!");
notification("Game Alert", "Something happened!"); // Windows notification
changeWallpaper("resources/backgrounds/creepy_bg.png"); // Fourth-wall breaking effect
website("https://example.com"); // Opens a web link
```

### Sub-Windows (Multi-Window Support)
Create secondary floating windows that contain their own backgrounds and characters.

*   **`createSubWindow(id, title, w, h)`**: Spawns a new window.
*   **`subWindowBg(id, imageName)`**: Sets background for sub-window.
*   **`showInSubWindow(id, name, image, x, y, [scale])`**: Shows character in sub-window.
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

1.  Build the JAR:
    ```bash
    # Windows
    gradlew.bat jar

    # Linux / macOS
    ./gradlew jar
    ```
2.  Find the JAR in `build/libs/`.
3.  Distribute the JAR along with the `resources/` folder.
4.  Players need **Java 17+** installed to run the game.
