# Java Visual Novel Engine

A simple Ren'Py-like visual novel engine written in Java using Swing.

## Features
- **Scripting in Java**: Write your stories as Java classes extending `GameScript`.
- **Backgrounds**: Support for loading images or solid colors.
- **Characters**: Sprite system.
- **Dialogue**: Typical name/text display.
- **Audio**: Background music and Sound Effects.
- **Resources**: Organized asset loading.

## How to Run

1. **Compile**:
   ```
   javac -d bin -sourcepath src src/com/vnengine/Main.java
   ```

2. **Run**:
   ```
   java -cp bin com.vnengine.Main
   ```
   *Or just run `run.bat` on Windows.*

## Writing a Story

Create a class in `src/com/vnengine/game/` extending `GameScript`:

```java
import com.vnengine.script.GameScript;

public class MyNewStory extends GameScript {
    @Override
    public void run() {
        playMusic("happy_vibes");
        
        scene("bg_classroom");
        show("Alice", "alice_happy");
        say("Alice", "Hello!");
        
        playSound("surprise");
        int choice = menu("Do you like Java?", "Yes", "No");
        
        if (choice == 0) {
            say("Alice", "Me too!");
        } else {
            say("Alice", "Oh...");
        }
        
        stopMusic();
    }
}
```

## Resources Structure
Place your assets in the `resources` folder:
*   `resources/backgrounds/`: Background images (jpg/png)
*   `resources/characters/`: Character sprites (png)
*   `resources/audio/music/`: Background music (wav/au/aiff)
*   `resources/audio/sfx/`: Sound effects (wav/au/aiff)

Note: Pure MP3 support requires external libraries (like JLayer), so stick to **WAV** for this vanilla implementation.
