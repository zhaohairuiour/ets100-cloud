# Fe Cloud · ETS100 云端答案阅读器（改版）

基于 Kotlin + Jetpack Compose 的 ETS100（e听说）作业答案阅读客户端。本仓库为**云端读取改造版**：仅保留 ETS100 官方云端 API 链路，移除第三方账户系统与全部本地读取模式。

## 版本

| 项 | 值 |
|---|---|
| applicationId | `com.shuaiqiu.fuckets100` |
| versionName | `2.3.0.1-fix` |
| versionCode | `25` |
| minSdk / targetSdk | 26 / 37 |
| 签名 | Android Debug（SHA-256 `79186029…d9c5`） |

## 改造范围

**已移除**

- 第三方账户系统（`data:account`、`feature:account` 两个 Gradle 模块整体删除）：账户登录、个人中心、绑定、云端授权、头像、验证码。
- 三种本地读取模式：Shizuku、Root、漏洞直读。对应移除激活面板、引导卡片、首页状态、本地加载器与本地目录导出、Shizuku/Sui 运行时初始化及 `shizuku-api` 依赖。

**已保留**

- ETS100 账号登录：`ETS100AuthManager`（手机号 + 密码，设备绑定短信验证）、畅言网页登录 Activity。
- 云端读取全链路：`ETS100ApiClient`、作业列表、下载、模板合并、答案解析、`PaperSource(Cloud)`。
- Native 解析组件：`libresolver.so`、`libcloud.so`（`app/src/main/jniLibs/arm64-v8a`）。

## 模块结构

```
app                  应用入口、导航、主题、Changyan 登录 Activity
core/{common,model,designsystem,settings,platform,navigation}
data/{content,ets100}
feature/{activation,onboarding,home,read,settings,shared}
baselineprofile
```

## 动画

引导页与页面过渡统一使用 `Animatable` + `graphicsLayer` 在 draw phase 直读，避免逐帧重组：

- 欢迎页：logo spring 缩放、标题 160ms 淡入、特性 340ms 上滑、CTA 560ms 上滑。
- 运行模式页 / 完成页：三段 stagger 上滑淡入。
- 步骤指示条：连续进度条 `scaleX` 插值 + 文案淡入切换。
- 步骤切换：`slideInHorizontally` + `fadeIn`，`SizeTransform(clip = false)` 消除高度插值裁剪。
- 阅读页：四态（gate/loading/error/list）入场容器 `StaggerEntrance`；列表项按 index 阶梯入场（45ms 步进，上限 8）。
- onboarding → 主界面：alpha 0→1 淡入。

## 构建

```bash
export JAVA_HOME=/path/to/jdk-17
export ANDROID_HOME=/path/to/android-sdk
./gradlew :app:assembleDebug
```

产物：`app/build/outputs/apk/debug/app-debug.apk`。

## 介绍站（GitHub Pages）

站点源码位于 `docs/`，纯静态、无构建步骤，由 `.github/workflows/pages.yml` 在每次 `main` 分支 `docs/**` 变更时自动发布。

```bash
# 本地预览
python3 -m http.server 8899 --directory docs
# 打开 http://127.0.0.1:8899/
```

仓库名为 `ets100-cloud` 时，Pages 地址为 `https://zhaohairuiour.github.io/ets100-cloud/`。首次部署需在仓库 `Settings → Pages → Build and deployment → Source` 选择 **GitHub Actions**。

站点结构：主题切换（深/浅，localStorage 记忆）、滚动进度、打字机副标题、滚动计数、卡片 spotlight、改造对比 Tab、架构节点联动、动画节奏可视化（1×/2×/4× 重播）、命令复制、`version.json` 多源回退校验、FAQ 折叠，并带 `404.html`、`manifest.webmanifest` 与 192/512 图标。

## 版本清单

根目录 `version.json` 供 App 与站点共同消费；`docs/version.json` 是站点侧同源副本，两者需保持一致（`apkUrl` 在发布 Release 后填入）。

## 许可与来源

本项目为他人项目的本地改造版本，原始项目声明源码闭源并禁止重新打包分发。**请勿将此仓库源码或构建产物公开分发**，仅作个人学习与备份使用；如需二次分发，需自行取得原始作者授权。
