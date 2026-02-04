@echo off
echo ===========================================
echo       Java Visual Novel Engine Builder
echo ===========================================
echo.

set BUILD_DIR=build
set DIST_DIR=dist
set JAR_NAME=Game.jar

echo [1/5] Cleaning up old build files...
if exist %BUILD_DIR% rmdir /s /q %BUILD_DIR%
if exist %DIST_DIR% rmdir /s /q %DIST_DIR%
mkdir %BUILD_DIR%
mkdir %DIST_DIR%

echo [2/5] Compiling Java sources...
pushd src
javac -d ..\build -sourcepath . com\vnengine\Main.java com\vnengine\core\*.java com\vnengine\game\*.java com\vnengine\script\*.java com\vnengine\tools\*.java com\vnengine\ui\*.java com\vnengine\util\*.java
popd


echo [3/5] Packaging JAR file...
echo Main-Class: com.vnengine.Main> manifest.txt
jar cfm %DIST_DIR%\%JAR_NAME% manifest.txt -C %BUILD_DIR% .
del manifest.txt

echo [4/5] Copying Assets...
xcopy /E /I /Y "resources" "%DIST_DIR%\resources"

echo [5/5] Creating Launcher...
echo @echo off > "%DIST_DIR%\Play.bat"
echo java -jar Game.jar >> "%DIST_DIR%\Play.bat"
echo pause >> "%DIST_DIR%\Play.bat"

echo   - Creating Linux Launcher (Play.sh)...
echo #!/bin/sh > "%DIST_DIR%\Play.sh"
echo java -jar Game.jar >> "%DIST_DIR%\Play.sh"

echo   - Compiling EXE launcher...
set CSC=""
if exist "C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe" set CSC="C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe"
if exist "C:\Windows\Microsoft.NET\Framework\v4.0.30319\csc.exe" set CSC="C:\Windows\Microsoft.NET\Framework\v4.0.30319\csc.exe"

if not %CSC%=="" (
    %CSC% /target:winexe /out:"%DIST_DIR%\Game.exe" /r:System.Windows.Forms.dll "src\Launcher.cs"
    echo   - Game.exe created!
) else (
    echo   - C# compiler not found, skipping EXE creation.
)

echo.
echo ===========================================
echo            Build Complete!
echo ===========================================
echo.
echo You can find your game in the 'dist' folder.
echo To distribute, zip the 'dist' folder and verify 'Play.bat' works.
echo.
echo.
