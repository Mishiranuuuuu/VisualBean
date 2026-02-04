@echo off
echo Fixing Git synchronization...

:: 1. Back up your scripts (just in case git pull deletes them)
if exist update_repo.bat copy update_repo.bat update_repo.bat.bak >nul
if exist setup_git.bat copy setup_git.bat setup_git.bat.bak >nul
if exist fix_window.py copy fix_window.py fix_window.py.bak >nul
if exist HowToCreateYourGame.md copy HowToCreateYourGame.md HowToCreateYourGame.md.bak >nul
if exist untrack_files.bat copy untrack_files.bat untrack_files.bat.bak >nul

:: 2. Pull changes from GitHub (this downloads the "deletion" you did online)
echo Pulling changes from GitHub...
git pull origin main --no-rebase

:: 3. Restore files if they were deleted by the pull
if not exist update_repo.bat if exist update_repo.bat.bak move update_repo.bat.bak update_repo.bat >nul
if not exist setup_git.bat if exist setup_git.bat.bak move setup_git.bat.bak setup_git.bat >nul
if not exist fix_window.py if exist fix_window.py.bak move fix_window.py.bak fix_window.py >nul
if not exist HowToCreateYourGame.md if exist HowToCreateYourGame.md.bak move HowToCreateYourGame.md.bak HowToCreateYourGame.md >nul
if not exist untrack_files.bat if exist untrack_files.bat.bak move untrack_files.bat.bak untrack_files.bat >nul

:: Cleanup backups
if exist update_repo.bat.bak del update_repo.bat.bak
if exist setup_git.bat.bak del setup_git.bat.bak
if exist fix_window.py.bak del fix_window.py.bak
if exist HowToCreateYourGame.md.bak del HowToCreateYourGame.md.bak
if exist untrack_files.bat.bak del untrack_files.bat.bak

:: 4. Ensure they are untracked locally
git rm --cached update_repo.bat setup_git.bat fix_window.py HowToCreateYourGame.md untrack_files.bat 2>nul
git add .
git commit -m "Merged remote changes and ensured scripts are ignored"

:: 5. Push everything back
echo Pushing...
git push origin main

echo Done!
pause
