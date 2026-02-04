@echo off
if not exist bin mkdir bin
javac -d bin -sourcepath src src/com/vnengine/Main.java
if %errorlevel% neq 0 exit /b %errorlevel%
java -cp bin com.vnengine.Main
