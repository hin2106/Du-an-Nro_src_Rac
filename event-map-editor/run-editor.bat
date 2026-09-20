@echo off
setlocal
cd /d "%~dp0"
if not exist "target\event-map-editor-1.0.0.jar" (
    echo Dang build Event Map Editor...
    call mvn -q -DskipTests package
    if errorlevel 1 pause & exit /b 1
)
java -jar "target\event-map-editor-1.0.0.jar"
if errorlevel 1 pause
