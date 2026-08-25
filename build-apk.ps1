# 彩记 App 构建脚本 - 生成并覆盖 APK
$ErrorActionPreference = "Stop"

$JAVA_HOME = "D:\andriod_studio\jbr"
$env:JAVA_HOME = $JAVA_HOME
$env:Path = "$JAVA_HOME\bin;" + [Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [Environment]::GetEnvironmentVariable("Path","User")

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  彩记 App 构建脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "[1/4] 清理旧构建..." -ForegroundColor Yellow
& .\gradlew.bat :app:clean --console=plain 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) { Write-Host "[错误] 清理失败" -ForegroundColor Red; Read-Host "按回车退出"; exit 1 }

Write-Host ""
Write-Host "[2/4] 编译 Debug APK..." -ForegroundColor Yellow
& .\gradlew.bat :app:assembleDebug --console=plain 2>&1 | Select-String -Pattern "BUILD" | ForEach-Object { Write-Host $_.Line }
if ($LASTEXITCODE -ne 0) { Write-Host "[错误] 编译失败" -ForegroundColor Red; Read-Host "按回车退出"; exit 1 }

Write-Host ""
Write-Host "[3/4] 检查 APK 是否生成..." -ForegroundColor Yellow
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (-not (Test-Path $apkPath)) {
    Write-Host "[错误] APK 文件未找到: $apkPath" -ForegroundColor Red
    Read-Host "按回车退出"; exit 1
}
Write-Host "APK 已生成: $apkPath" -ForegroundColor Green

Write-Host ""
Write-Host "[4/4] 复制 APK 到项目根目录（覆盖旧版本）..." -ForegroundColor Yellow
Copy-Item -Path $apkPath -Destination "app-debug.apk" -Force
if (-not (Test-Path "app-debug.apk")) {
    Write-Host "[错误] 复制 APK 失败" -ForegroundColor Red
    Read-Host "按回车退出"; exit 1
}

$size = [math]::Round((Get-Item "app-debug.apk").Length / 1KB, 1)
Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  构建成功！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "APK 位置:" -ForegroundColor White
Write-Host "  1. 项目根目录: d:\彩记\app-debug.apk ($size KB)"
Write-Host "  2. 原始构建目录: d:\彩记\app\build\outputs\apk\debug\app-debug.apk"
Write-Host ""

# 检测设备并提示安装
$devices = & adb devices 2>$null | Select-String "device$"
if ($devices) {
    Write-Host "检测到已连接的设备:" -ForegroundColor Yellow
    & adb devices
    Write-Host ""
    $install = Read-Host "是否立即安装到设备？(y/n)"
    if ($install -eq "y" -or $install -eq "Y") {
        Write-Host "正在安装..." -ForegroundColor Yellow
        & adb install -r $apkPath
        if ($LASTEXITCODE -eq 0) {
            Write-Host "安装成功！" -ForegroundColor Green
        } else {
            Write-Host "[错误] 安装失败" -ForegroundColor Red
        }
    }
}

Write-Host ""
Read-Host "按回车退出"
