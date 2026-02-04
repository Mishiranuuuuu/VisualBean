@echo off
echo Setting up Git repository...

:: Initialize Git
git init

:: Add all files
git add .

:: Commit
git commit -m "Initial commit of Visual Novel Engine"
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ========================================================
    echo Commit failed!
    echo It likely looks like you haven't configured your Git Identity yet.
    echo Please run the following commands in your terminal:
    echo.
    echo git config --global user.email "your-email@example.com"
    echo git config --global user.name "Your Name"
    echo.
    echo Then run this script again.
    echo ========================================================
    pause
    exit /b
)

:: Rename branch to main
git branch -M main

:: Add remote (ignore error if it already exists)
git remote add origin git@github.com:Mishiranuuuuu/VisualBean.git

:: Push to GitHub
echo.
echo Pushing to GitHub...
git push -u origin main

echo.
echo Done!
pause
