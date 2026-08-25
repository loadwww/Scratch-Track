# Scratch Track (彩记)

一款专为彩票爱好者打造的记账应用，帮助你记录每次购彩的投入与中奖，追踪盈亏趋势，并提供虚拟刮刮乐娱乐体验。支持 Android 原生 App 和 Web App (PWA) 双端使用。

## 在线体验 (Web 版)

**访问地址：https://loadwww.github.io/Scratch-Track/**

### iPhone / Safari 使用方法
1. 用 Safari 打开上方链接
2. 点击底部分享按钮 → 滚动找到"添加到主屏幕"
3. 桌面会出现"彩记"图标，点击即可全屏使用

### 离线数据存储
- 所有数据存储在浏览器本地（IndexedDB + localStorage），无需联网
- Service Worker 缓存页面资源，首次加载后离线也能正常打开使用
- 支持导出/导入 JSON 备份文件，方便换设备迁移数据

## 功能概览

### 首页仪表盘
- 实时显示本月投入、中奖、盈亏金额及盈亏比例
- 月度预算进度条（绿/蓝/黄/深红四色分段提示）
- 可点击切换的轮换提醒文字（自定义多组句子，点击随机切换）
- 支持自定义壁纸背景图

### 彩票日记
- 记录每期投注的标题、文字描述和彩票图片
- 按时间顺序排列，支持删除（自动同步统计金额）
- 收藏功能：点击星星标记高光时刻，点击日记卡片可查看详情

### 中奖记录
- 快速记录投入金额与中奖金额
- 按月份筛选，支持按投入/盈利/盈利比排序
- 删除记录时自动同步首页统计数据

### 高光时刻
- 从彩票日记中收藏的精彩记录
- 点击卡片直接跳转查看对应日记详情
- 支持取消收藏和删除日记

### 历史盈亏图表
- 最近 6 个月投入/中奖双柱对比图（以 100 为单位）
- 最近 6 次记录的投入/中奖双柱对比图（以 50 为单位）
- 6 个月以上的数据以数据条形式展示

### 随机数生成
- 自定义最小值和最大值范围
- 支持幸运池功能：手动输入幸运号码，随机抽取

### 虚拟刮刮乐体验
- 每日登录自动领取虚拟金币（可在设置中自定义每日领取数量）
- 三种面值可选：20 / 30 / 50 金币
- 6 档奖级：1倍、2倍、5倍、10倍、20倍、100倍
- 整体返奖率约 65%，中奖率约 50%
- 模拟真实磨砂涂层刮开效果，滑动时触发"沙沙"摩擦音效
- 中奖金币自动累计，全程无真实金钱交易

### 设置
- 首页轮换文字（多组句子配置，每行一句）
- 背景主题切换 + 自定义壁纸
- 月度购彩预算设置
- 盈利庆祝音乐（内置《好运来》，支持自定义音频，盈利时自动播放）
- 虚拟刮刮乐每日领取金币数设置
- 本地/云盘数据备份与恢复

## 技术栈

| 分类 | 技术 |
|------|------|
| 语言 | Kotlin 2.0.21 |
| UI 框架 | Jetpack Compose (BOM 2024.10.00) |
| 架构 | MVVM + Clean Architecture |
| 数据库 | Room 2.6.1 |
| 偏好存储 | DataStore Preferences |
| 导航 | Navigation Compose 2.8.3 |
| 图片加载 | Coil Compose 2.7.0 |
| 异步 | Kotlin Coroutines + Flow |
| 数据备份 | FileProvider + SAF (Storage Access Framework) |
| 构建工具 | Gradle 8.10.2, AGP 8.7.3, KSP |

## 环境要求

- **Android Studio** Hedgehog 或更高版本
- **JDK 17**
- **Android SDK** compileSdk 36, minSdk 26 (Android 8.0)
- **Gradle** 8.10.2（项目已包含 Gradle Wrapper）

## 快速开始

### 1. 克隆仓库

```bash
git clone https://github.com/loadwww/Scratch-Track.git
cd Scratch-Track
```

### 2. 使用 Android Studio 打开

1. 打开 Android Studio
2. 选择 `File > Open`，选择项目根目录
3. 等待 Gradle 同步完成（首次同步会下载依赖，请确保网络畅通）

### 3. 构建并运行

**方式一：通过 Android Studio**

1. 连接 Android 设备或启动模拟器
2. 点击运行按钮（或 `Shift + F10`）

**方式二：通过命令行**

```bash
# Windows
.\gradlew.bat :app:assembleDebug

# macOS / Linux
./gradlew :app:assembleDebug
```

生成的 APK 位于：`app/build/outputs/apk/debug/app-debug.apk`

**方式三：使用一键构建脚本（Windows）**

```powershell
.\build-apk.ps1
```

> 注意：`build-apk.ps1` 中的 `JAVA_HOME` 路径为硬编码，请根据你的实际安装路径修改：
> ```powershell
> $JAVA_HOME = "你的Android Studio路径\jbr"
> ```

### 4. 安装到设备

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 项目结构

```
app/src/main/java/com/caiji/app/
├── MainActivity.kt              # 应用入口
├── CaiJiApplication.kt           # Application 类
├── data/
│   ├── ServiceLocator.kt         # 依赖注入容器
│   ├── backup/BackupManager.kt   # 数据备份管理
│   ├── db/                        # Room 数据库
│   │   ├── CaiJiDatabase.kt
│   │   ├── dao/                   # 数据访问对象
│   │   └── entity/               # 数据实体
│   ├── prefs/SettingsStore.kt    # DataStore 偏好设置
│   └── repository/               # 数据仓库层
├── ui/
│   ├── CaiJiApp.kt               # 导航配置
│   ├── home/                     # 首页
│   ├── diary/                    # 彩票日记
│   ├── highlight/                # 高光时刻
│   ├── lottery/                  # 中奖记录
│   ├── chart/                    # 历史盈亏图表
│   ├── random/                   # 随机数生成
│   ├── scratch/                  # 虚拟刮刮乐
│   ├── budget/                   # 月度预算
│   ├── settings/                 # 设置
│   ├── components/               # 共享 UI 组件
│   └── theme/                    # 主题/颜色/字体
└── util/                         # 工具类
    ├── DateUtils.kt
    ├── MoneyUtils.kt
    ├── MusicPlayer.kt
    └── ScratchSoundManager.kt
```

## 刮刮乐概率说明

刮刮乐采用加权随机算法，中奖率约 50%，整体返奖率约 65%。

中奖时各倍率的出现概率：

| 奖级 | 倍率 | 出现概率（中奖条件下） |
|------|------|------------------------|
| 一等奖 | 100 倍 | 0.05% |
| 二等奖 | 20 倍 | 0.18% |
| 三等奖 | 10 倍 | 0.32% |
| 四等奖 | 5 倍 | 1.45% |
| 五等奖 | 2 倍 | 13.50% |
| 六等奖 | 1 倍 | 84.50% |

期望奖金 = 面值 x 50% x (8450x1 + 1350x2 + 145x5 + 32x10 + 18x20 + 5x100) / 10000 = 面值 x 0.65

## 数据说明

- 所有数据均存储在本地设备，不涉及任何云端上传
- 支持通过系统文件选择器导出/导入备份文件
- 数据库采用 Room（SQLite），升级时使用 fallbackToDestructiveMigration 策略

## 许可证

本项目采用 MIT 许可证，详见 [LICENSE](LICENSE)。
