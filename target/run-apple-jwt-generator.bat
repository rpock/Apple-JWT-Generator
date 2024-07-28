@echo off
setlocal

rem Get the directory of this script
set "SCRIPT_DIR=%~dp0"

rem Run the JAR file
java -jar "%SCRIPT_DIR%applejwtgenerator-1.0-SNAPSHOT.jar"

rem Pause to keep the console window open if double-clicked
pause