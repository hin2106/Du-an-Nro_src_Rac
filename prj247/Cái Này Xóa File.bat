@echo off
setlocal ENABLEDELAYEDEXECUTION ENABLEEXTENSIONS

:: ============================================================
:: CleanUnityProject.bat
:: Dọn rác project Unity trước khi share/bán
:: Options:
::    /dryrun   : chỉ LIỆT KÊ, không xóa
::    /y        : auto đồng ý, không hỏi
::    /purgeVCS : xóa thêm file/thu mục của VCS/IDE (cẩn thận)
:: ============================================================

:: -------- Parse args --------
set DRYRUN=0
set YES=0
set PURGEVCS=0

for %%A in (%*) do (
    if /I "%%~A"=="/dryrun"   set DRYRUN=1
    if /I "%%~A"=="/y"        set YES=1
    if /I "%%~A"=="/purgeVCS" set PURGEVCS=1
)

:: -------- Check Unity project root --------
if not exist "Assets" (
    echo [!] Không thấy folder "Assets" => Có vẻ không phải gốc project Unity.
    echo     Hãy chạy file này tại thư mục chứa Assets/ Packages/ ProjectSettings/.
    exit /b 1
)
if not exist "ProjectSettings" (
    echo [!] Không thấy folder "ProjectSettings" => Có vẻ không phải gốc project Unity.
    exit /b 1
)

title Unity Cleaner

echo ============================================================
echo  Unity Cleaner - Safe share/sell
echo  DRYRUN = %DRYRUN% ^| AUTO-YES = %YES% ^| PURGE-VCS = %PURGEVCS%
echo ============================================================
echo.

:: -------- Define targets to remove --------
:: Core Unity caches/builds (safe to delete)
set FOLDERS=^
Library;^
Temp;^
obj;^
Build;^
Builds;^
Logs;^
UserSettings;^
MemoryCaptures;^
CrashReports;^
Artifacts;^
ShaderCache;^
IL2CPPBuildCache;^
Logs~;^
.Gradle;^
GradleCache;^
.apt_generated;^
.uvtmp;^
bin;^
Intermediate;^
*.xcworkspace;^
DerivedData

:: Extra IDE/editor junk (optional via PURGEVCS)
set FOLDERS_VCS=^
.vs;^
.idea;^
.gradle;^
.vscode\.history;^
.vscode\.ropeproject;^
.ReSharper;^
_UpgradeReport_Files;^
Backup;^
obj\Debug;^
obj\Release

:: Files to delete
set FILES=^
*.csproj;*.sln;*.user;*.unityproj;*.pidb;*.suo;*.tmp;*.log;*.dmp;*.bak;*.orig;*.TMP;*.DS_Store;Thumbs.db;desktop.ini;*.mdb;*.pdb

:: Files to delete (extra when /purgeVCS)
set FILES_VCS=^
*.iml;*.ipr;*.iws;*.DotSettings;*.DotSettings.user;*.resharper;*.ReSharper;*.code-workspace

:: -------- Pretty print plan --------
echo [Plan] Sẽ xoá các THƯ MỤC (nếu tồn tại):
for %%D in (%FOLDERS%) do echo   - %%D
if %PURGEVCS%==1 (
    for %%D in (%FOLDERS_VCS%) do echo   - %%D   (purgeVCS)
)
echo.
echo [Plan] Sẽ xoá các TỆP tin phù hợp mẫu:
for %%F in (%FILES%) do echo   - %%F
if %PURGEVCS%==1 (
    for %%F in (%FILES_VCS%) do echo   - %%F   (purgeVCS)
)
echo.

if %DRYRUN%==1 (
    echo [DRY RUN] Chỉ liệt kê, KHÔNG xoá gì. Kết thúc.
    exit /b 0
)

if %YES%==0 (
    choice /M "Tiếp tục xoá như trên?"
    if errorlevel 2 (
        echo Đã hủy.
        exit /b 0
    )
)

:: -------- Delete folders (safe set) --------
echo.
echo ===== Xoá thư mục core =====
for %%D in (%FOLDERS%) do (
    call :RMDIR_SAFE "%%~D"
)

:: -------- Delete folders (extra when PURGEVCS) --------
if %PURGEVCS%==1 (
    echo.
    echo ===== Xoá thư mục IDE/VCS (purgeVCS) =====
    for %%D in (%FOLDERS_VCS%) do (
        call :RMDIR_SAFE "%%~D"
    )
)

:: -------- Delete files (safe set) --------
echo.
echo ===== Xoá tệp theo mẫu =====
for %%F in (%FILES%) do (
    call :DEL_GLOB "%%~F"
)

:: -------- Delete files (extra when PURGEVCS) --------
if %PURGEVCS%==1 (
    echo.
    echo ===== Xoá tệp IDE/VCS (purgeVCS) =====
    for %%F in (%FILES_VCS%) do (
        call :DEL_GLOB "%%~F"
    )
)

echo.
echo ✅ Done. Bạn có thể mở Unity, nó sẽ tái tạo Library/Temp tự động.
exit /b 0

:: -------- Helpers --------
:RMDIR_SAFE
set "_TARGET=%~1"
if exist "%_TARGET%" (
    echo [DIR] Xoá "%_TARGET%"
    rmdir /s /q "%_TARGET%" 2>nul
) else (
    rem echo [DIR] Bỏ qua "%_TARGET%" (không tồn tại)
)
exit /b

:DEL_GLOB
set "_PATTERN=%~1"
for /r %%X in (%_PATTERN%) do (
    if exist "%%~fX" (
        echo [FILE] Xoá "%%~fX"
        del /f /q "%%~fX" 2>nul
    )
)
exit /b
