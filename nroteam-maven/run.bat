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

rem ===== JVM Performance Tuning =====
rem Heap: 1GB initial, 2GB max (tăng -Xmx nếu VPS có nhiều RAM)
rem GC: G1GC cho latency thấp, max pause 50ms
rem AlwaysPreTouch: cấp phát memory ngay khi start, tránh lag lúc chạy
set "JVM_OPTS=-Xms1g -Xmx2g"
set "GC_OPTS=-XX:+UseG1GC -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=4m -XX:+ParallelRefProcEnabled"
set "PERF_OPTS=-XX:+AlwaysPreTouch -XX:+OptimizeStringConcat -XX:-UseBiasedLocking"
set "NET_OPTS=-Dio.netty.leakDetection.level=disabled -Dio.netty.recycler.maxCapacityPerThread=0"
set "GC_LOG=-Xlog:gc*:file=logs/gc.log:time,uptime,level,tags:filecount=5,filesize=10m"

echo [INFO] JVM: %JVM_OPTS% %GC_OPTS%
"%JAVA_EXE%" %JVM_OPTS% %GC_OPTS% %PERF_OPTS% %NET_OPTS% %GC_LOG% -jar "%JAR_FILE%" %*

pause
