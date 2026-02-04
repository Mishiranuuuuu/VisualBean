@echo off
git rm --cached update_repo.bat setup_git.bat fix_window.py HowToCreateYourGame.md untrack_files.bat
git commit -m "Stop tracking script files as per gitignore"
del untrack_files.bat
