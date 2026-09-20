@echo off
REM Script để xóa thư mục lib/ sau khi đã migrate sang Maven
REM Chạy script này sau khi đã verify Maven build thành công

echo ========================================
echo CLEANUP LIB FOLDER - MIGRATION TO MAVEN
echo ========================================
echo.
echo WARNING: Script này sẽ xóa thư mục lib/
echo Đảm bảo bạn đã:
echo   1. Build project với Maven thành công: mvn clean install
echo   2. Test application chạy được: mvn spring-boot:run
echo   3. Backup lib/ folder nếu cần
echo.
pause

REM Kiểm tra xem có thư mục lib không
if not exist "lib" (
    echo Thư mục lib/ không tồn tại!
    pause
    exit /b
)

REM Tạo backup (optional)
echo.
echo Tạo backup lib/ folder...
if not exist "backup" mkdir backup
xcopy /E /I /Y lib backup\lib_backup_%date:~-4,4%%date:~-7,2%%date:~-10,2%_%time:~0,2%%time:~3,2%%time:~6,2%

REM Xóa thư mục lib
echo.
echo Đang xóa thư mục lib/...
rmdir /S /Q lib

if not exist "lib" (
    echo.
    echo ========================================
    echo SUCCESS: Thư mục lib/ đã được xóa!
    echo ========================================
    echo.
    echo Backup được lưu tại: backup\lib_backup_*
    echo.
) else (
    echo.
    echo ERROR: Không thể xóa thư mục lib/!
    echo.
)

pause

