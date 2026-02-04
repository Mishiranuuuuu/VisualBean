@echo off
if not exist bin mkdir bin
javac -d bin -sourcepath src src/com/vnengine/Main.java
if %errorlevel% neq 0 (
    echo COMPILATION FAILED
) else (
    echo COMPILATION SUCCESS
)
