@echo off
cd /d "%~dp0\..\.."

echo ===========================================
echo        VisualBean Save Inspector
echo ===========================================
echo.

if not exist bin mkdir bin

echo [1/2] Compiling Tools...
javac -d bin -sourcepath src src/com/vnengine/tools/SaveInspector.java src/com/vnengine/core/SaveData.java
if errorlevel 1 (
    echo.
    echo [ERROR] Compilation failed! 
    echo Please make sure you have the JDK installed and 'javac' is in your PATH.
    pause
    exit /b
)

echo.
echo [2/2] Inspecting Saves...
echo.
java -cp bin com.vnengine.tools.SaveInspector

echo.
pause
