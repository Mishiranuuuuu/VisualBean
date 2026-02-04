# VisualBaen Engine

A powerful, lightweight, and customizable engine for creating Visual Novels using Java and Swing. Designed to be easy to use for developers while offering deep customization through CSS-like theming and Java-based scripting.

## Features

*   **Native Java Scripting**: Write your game logic using standard Java. Use loops, conditionals, and variables freely.
*   **Dynamic UI Styling**: Customize every aspect of the interface (Dialog boxes, Menus, Buttons) using a simple `theme.css` file.
*   **Rich Visuals**:
    *   Background and Character Sprite support (PNG/JPG).
    *   **Sub-Windows**: Create multiple floating windows for unique storytelling elements.
    *   **Animations**: Smooth window centering, scaling, and character transitions.
*   **Robust Save System**:
    *   9 Save Slots with visual previews.
    *   Save/Load game state functionality.
    *   Delete save files directly from the UI.
    *   Persists window positions and preferences.
*   **Audio**: Support for Background Music (BGM) and Sound Effects (SFX) (WAV/AU formats).
*   **Window Management**: Custom window titles, resizable windows, and animated interactions.

## Getting Started

### Prerequisites

*   **Java Development Kit (JDK) 8** or higher installed.

### Installation

1.  Clone this repository:
    ```bash
    git clone https://github.com/yourusername/java-vn-engine.git
    cd java-vn-engine
    ```
2.  (Optional) Run `setup_git.bat` to initialize the local environment if needed.

## Running the Engine

We provide several batch scripts to make your life easier:

*   **`run.bat`**: Compiles and runs the current project. This is your main entry point.
*   **`compile_and_run_demo.bat`**: Specifically for testing the demo inclusions.
*   **`build_game.bat`**: Compiles the game into a standalone `dist` folder ready for distribution.

## Creating Your Story

For a detailed guide on creating your own Visual Novel, please refer to **[HowToCreateYourGame.md](HowToCreateYourGame.md)** included in this repository.

### Quick Example

Scripts are located in `src/com/vnengine/game/`. Inherit from `GameScript` to start:

```java
public class MyStory extends GameScript {
    @Override
    public void run() {
        scene("classroom");
        playMusic("bgm_happy");
        
        show("Alice", "smile");
        say("Alice", "Hi! Welcome to the Java VN Engine.");
        
        int choice = menu("Ready to start?", "Yes!", "Not yet.");
        if (choice == 0) {
            say("Alice", "Great! Let's go!");
        }
    }
}
```

## Theming

Modify `resources/theme.css` to change the look of your game instantly:

```css
.dialog-box {
    background-color: #222222;
    text-color: #FFFFFF;
    font-family: "Segoe UI";
    opacity: 200;
}
```

## Build for Release

To package your game for players:
1.  Double-click `build_game.bat`.
2.  Find your game in the new `dist/` folder.
3.  Share the `dist/` folder! It contains the executable jar and all resources.

## License

MIT License
