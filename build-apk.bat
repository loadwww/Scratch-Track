@echo off
chcp 65001 >nul
REM ============================================================
REM  彩记 App 构建脚本 - 生成并覆盖 APK
REM ============================================================
setlocal

set JAVA_HOME=D:\andriod_studio\jbr
set PATH=%JAVA_HOME%\bin;%PATH%

echo.
echo ========================================
echo   彩记 App 构建脚本
echo ========================================
echo.

echo [1/4] 清理旧构建...
call gradlew.bat :app:clean --console=plain
if errorlevel 1 (
    echo [错误] 清理失败
    pause
    exit /b 1
)

echo.
echo [2/4] 编译 Debug APK...
call gradlew.bat :app:assembleDebug --console=plain
if errorlevel 1 (
    echo [错误] 编译失败
    pause
    exit /b 1
)

echo.
echo [3/4] 检查 APK 是否生成...
set APK_PATH=app\build\outputs\apk\debug\app-debug.apk
if not exist "%APK_PATH%" (
    echo [错误] APK 文件未找到: %APK_PATH%
    pause
    exit /b 1
)

echo APK 已生成: %APK_PATH%

echo.
echo [4/4] 复制 APK 到项目根目录（覆盖旧版本）...
copy /Y "%APK_PATH%" "app-debug.apk" >nul
if errorlevel 1 (
    echo [错误] 复制 APK 失败
    pause
    exit /b 1
)

echo.
echo ========================================
echo   构建成功！
echo ========================================
echo.
echo APK 位置:
echo   1. 项目根目录: d:\彩记\app-debug.apk
echo   2. 原始构建目录: d:\彩记\app\build\outputs\apk\debug\app-debug.apk
echo.

REM 如果有连接的设备/模拟器，提示是否安装
adb devices 2>nul | findstr "device" >nul
if not errorlevel 1 (
    echo 检测到已连接的设备:
    adb devices
    echo.
    set /p install="是否立即安装到设备？(y/n): "
    if /i "%install%"=="y" (
        echo 正在安装...
        adb install -r "%APK_PATH%"
        if errorlevel 1 (
            echo [错误] 安装失败
        ) else (
            echo 安装成功！
        )
    )
)

echo.
pause
