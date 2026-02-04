@echo off
setlocal

:: Move to project root (up 2 levels from scripts/Windows/)
pushd "%~dp0\..\.."

echo ===========================================
echo       Java Visual Novel Engine
echo ===========================================
echo.

echo [1/2] Compiling Engine...
if not exist bin mkdir bin

:: Check for Java
where javac >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java Compiler (javac) not found!
    echo Please install the JDK (Java Development Kit) and add it to your PATH.
    pause
    popd
    exit /b 1
)

javac -d bin -sourcepath src src/com/vnengine/Main.java
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation Failed! Please check your code for errors.
    pause
    popd
    exit /b 1
)

echo [2/2] Running Game...
echo.
java -cp bin com.vnengine.Main
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Game crashed or exited with an error.
)

popd
echo.
pause
