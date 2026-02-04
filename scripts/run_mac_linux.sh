#!/bin/bash
# Move to project root
cd "$(dirname "$0")/.."

mkdir -p bin
javac -d bin -sourcepath src src/com/vnengine/Main.java
java -cp bin com.vnengine.Main
