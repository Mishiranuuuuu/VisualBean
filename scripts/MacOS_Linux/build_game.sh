#!/bin/bash

# Move to project root (up 2 levels from scripts/MacOS_Linux/)
cd "$(dirname "$0")/../.." || exit

echo "==========================================="
echo "      Java Visual Novel Engine Builder"
echo "==========================================="
echo ""

BUILD_DIR="build"
DIST_DIR="dist"
JAR_NAME="Game.jar"

# 1. Cleanup
echo "[1/4] Cleaning up old build files..."
rm -rf "$BUILD_DIR" "$DIST_DIR"
mkdir -p "$BUILD_DIR"
mkdir -p "$DIST_DIR"

# 2. Compile Java
echo "[2/4] Compiling Java sources..."
if ! command -v javac &> /dev/null; then
    echo "[ERROR] javac not found! Please install JDK."
    exit 1
fi

javac -d "$BUILD_DIR" -sourcepath src src/com/vnengine/Main.java src/com/vnengine/core/*.java src/com/vnengine/game/*.java src/com/vnengine/script/*.java src/com/vnengine/ui/*.java src/com/vnengine/util/*.java

if [ $? -ne 0 ]; then
    echo "[ERROR] Compilation failed!"
    exit 1
fi

# 3. Package JAR
echo "[3/4] Packaging JAR file..."
echo "Main-Class: com.vnengine.Main" > "$BUILD_DIR/manifest.txt"
jar cfm "$DIST_DIR/$JAR_NAME" "$BUILD_DIR/manifest.txt" -C "$BUILD_DIR" .

if [ $? -ne 0 ]; then
    echo "[ERROR] Failed to package JAR."
    rm "$BUILD_DIR/manifest.txt"
    exit 1
fi
rm "$BUILD_DIR/manifest.txt"

# 4. Copy Assets
echo "[4/4] Copying Assets..."
if [ -d "resources" ]; then
    cp -r "resources" "$DIST_DIR/resources"
else
    echo "[WARNING] No 'resources' folder found! Game might miss assets."
fi

# 5. Create Launchers
echo "[INFO] Creating Launchers..."

# Linux/Mac Launcher
cat <<EOF > "$DIST_DIR/Play.sh"
#!/bin/sh
java -jar $JAR_NAME
EOF
chmod +x "$DIST_DIR/Play.sh"

# Windows Launcher (for convenience if distributing across platforms)
cat <<EOF > "$DIST_DIR/Play.bat"
@echo off
java -jar $JAR_NAME
pause
EOF

echo ""
echo "==========================================="
echo "           Build Complete!"
echo "==========================================="
echo ""
echo "Your game is ready in the 'dist' folder."
echo ""
