@echo off
setlocal
cd /d "%~dp0"
if exist "MewnBase.exe" (
  start "" /wait "%~dp0MewnBase.exe"
) else (
  java -Xmx2G -jar "game\desktop-1.0.jar"
)
if errorlevel 1 pause
