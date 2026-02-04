#!/bin/bash
# Move to project root (up 2 levels from scripts/MacOS_Linux/)
cd "$(dirname "$0")/../.."

mkdir -p bin
javac -d bin -sourcepath src src/com/vnengine/Main.java
java -cp bin com.vnengine.Main
