@echo off
setlocal
cd /d "%~dp0"
call mvn -DskipTests package
pause
