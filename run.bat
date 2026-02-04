@echo off
if not exist bin mkdir bin
javac -d bin -sourcepath src src/com/vnengine/Main.java
java -cp bin com.vnengine.Main
pause
