@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

set "ROOT_DIR=%~dp0"
cd /d "%ROOT_DIR%"
set "JAR_FILE=%ROOT_DIR%target\nro-server-1.0.0.jar"

if not exist "%JAR_FILE%" (
	echo [ERROR] Jar file not found: %JAR_FILE%
	echo Build the project first or upload a packaged target folder.
	pause
	exit /b 1
)

set "BOOTSTRAP_SCRIPT=%ROOT_DIR%bootstrap-java.ps1"
if not exist "%BOOTSTRAP_SCRIPT%" (
	echo [ERROR] Missing bootstrap script: %BOOTSTRAP_SCRIPT%
	pause
	exit /b 1
)

for /f "usebackq delims=" %%i in (`powershell -NoProfile -ExecutionPolicy Bypass -File "%BOOTSTRAP_SCRIPT%"`) do (
	set "JAVA_EXE=%%i"
)

if not defined JAVA_EXE (
	echo [ERROR] Unable to resolve Java runtime.
	pause
	exit /b 1
)

if not exist "%JAVA_EXE%" (
	echo [ERROR] java.exe not found: %JAVA_EXE%
	pause
	exit /b 1
)

echo [INFO] Using Java: %JAVA_EXE%
"%JAVA_EXE%" -jar "%JAR_FILE%"

pause