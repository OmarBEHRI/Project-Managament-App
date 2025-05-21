@echo off
echo Cleaning project...

REM Delete build directories
rmdir /S /Q "%~dp0build"
rmdir /S /Q "%~dp0app\build"

REM Delete Gradle cache
rmdir /S /Q "%USERPROFILE%\.gradle\caches\modules-2\files-2.1\com.android.tools.build"

REM Run Gradle clean with daemon stopped
cd "%~dp0"
call gradlew --stop
call gradlew clean --no-daemon

echo.
echo Building project with non-incremental build...
echo.

REM Run build with special flags
call gradlew assembleDebug --no-daemon --rerun-tasks

echo.
echo Build completed.
echo.
pause