@echo off
setlocal enabledelayedexpansion

:: Move to project root (up 2 levels from scripts/Windows/)
pushd "%~dp0\..\.."

echo ===========================================
echo       Java Visual Novel Engine Builder
echo ===========================================
echo.

set BUILD_DIR=build
set DIST_DIR=dist
set JAR_NAME=Game.jar

:: 1. Cleanup
echo [1/5] Cleaning up old build files...
if exist %BUILD_DIR% rmdir /s /q %BUILD_DIR%
if exist %DIST_DIR% rmdir /s /q %DIST_DIR%
mkdir %BUILD_DIR%
mkdir %DIST_DIR%

:: 2. Compile Java
echo [2/5] Compiling Java sources...
where javac >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] javac not found! Install JDK.
    pause
    exit /b 1
)

pushd src
javac -d ..\build -sourcepath . com\vnengine\Main.java com\vnengine\core\*.java com\vnengine\game\*.java com\vnengine\script\*.java com\vnengine\ui\*.java com\vnengine\util\*.java
if %ERRORLEVEL% NEQ 0 (
    popd
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)
popd

:: 3. Package JAR
echo [3/5] Packaging JAR file...
echo Main-Class: com.vnengine.Main> %BUILD_DIR%\manifest.txt
jar cfm %DIST_DIR%\%JAR_NAME% %BUILD_DIR%\manifest.txt -C %BUILD_DIR% .
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Failed to package JAR.
    del %BUILD_DIR%\manifest.txt
    pause
    exit /b 1
)
del %BUILD_DIR%\manifest.txt

:: 4. Copy Assets
echo [4/5] Copying Assets...
if exist "resources" (
    xcopy /E /I /Y "resources" "%DIST_DIR%\resources" >nul
) else (
    echo [WARNING] No 'resources' folder found! Game might miss assets.
)

:: 5. Create Launchers
echo [5/5] Creating Launchers...

:: Batch Launcher
(
echo @echo off
echo java -jar Game.jar
echo pause
) > "%DIST_DIR%\Play.bat"

:: Linux Launcher
(
echo #!/bin/sh
echo java -jar Game.jar
) > "%DIST_DIR%\Play.sh"

:: Optional: C# EXE Launcher (Requires .NET Framework)
echo    - Attempting to build EXE launcher...
set "CSC="

:: Find C# Compiler
for %%p in (
   "C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe"
   "C:\Windows\Microsoft.NET\Framework\v4.0.30319\csc.exe"
) do (
   if exist %%p set "CSC=%%~p"
)

if defined CSC (
    if exist "src\Launcher.cs" (
        "%CSC%" /target:winexe /out:"%DIST_DIR%\Game.exe" /r:System.Windows.Forms.dll "src\Launcher.cs" >nul
        if !ERRORLEVEL! EQU 0 (
             echo      [SUCCESS] Game.exe created!
        ) else (
             echo      [WARNING] C# Compilation failed.
        )
    ) else (
        echo      [INFO] src\Launcher.cs not found, skipping EXE.
    )
) else (
    echo      [INFO] C# Compiler not found, skipping EXE.
)

echo.
echo ===========================================
echo            Build Complete!
echo ===========================================
echo.
echo Your game is ready in the 'dist' folder.
echo.
popd
pause
