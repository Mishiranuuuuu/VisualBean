#!/bin/bash

cd "$(dirname "$0")/../.." || exit

echo "==========================================="
echo "       VisualBean Save Inspector"
echo "==========================================="
echo ""

mkdir -p bin

echo "[1/2] Compiling Tools..."
if ! command -v javac &> /dev/null; then
    echo "[ERROR] javac not found! Please install JDK."
    exit 1
fi

javac -d bin -sourcepath src src/com/vnengine/tools/SaveInspector.java src/com/vnengine/core/SaveData.java

if [ $? -ne 0 ]; then
    echo ""
    echo "[ERROR] Compilation Failed!"
    exit 1
fi

echo "[2/2] Inspecting Saves..."
echo ""
java -cp bin com.vnengine.tools.SaveInspector

echo ""
