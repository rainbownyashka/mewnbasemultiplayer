@echo off
setlocal EnableExtensions
set SAVE=rand_%DATE:~-4%%DATE:~4,2%%DATE:~7,2%_%TIME:~0,2%%TIME:~3,2%%TIME:~6,2%
set SAVE=%SAVE: =0%
echo Starting new random world: %SAVE%
java -Dmewnbase.save=%SAVE% -jar basegame\game\desktop-1.0.jar
pause
