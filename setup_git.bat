@echo off
echo Setting up Git repository...

:: Initialize Git (safe to run even if already initialized)
git init

:: Add all files
git add .

:: Commit
git commit -m "Initial commit of Visual Novel Engine"

:: Rename branch to main
git branch -M main

:: Add remote (ignore error if it already exists)
git remote remove origin
git remote add origin https://github.com/Mishiranuuuuu/VisualBean.git

:: Push to GitHub
echo.
echo Pushing to GitHub...
echo (A browser window or login prompt might appear now)
git push -u origin main

echo.
echo Done!
pause
