@echo off
cd /d "C:\FRC - PROGRAMACAO\teste_swervedrive\dashboard-bridge\py"
start "" py main.py
timeout /t 3 >nul
start "" "C:\FRC - PROGRAMACAO\teste_swervedrive\dashboard-web\index.html"
start "" "C:\FRC - PROGRAMACAO\teste_swervedrive\dashboard-web\limelight.html"
exit