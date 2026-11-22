# Fe Cloud · ETS100 云端答案阅读器（改版）

基于 Kotlin + Jetpack Compose 的 ETS100（e听说）作业答案阅读客户端。本仓库为**云端读取改造版**：仅保留 ETS100 官方云端 API 链路，移除第三方账户系统与全部本地读取模式。

## 版本

| 项 | 值 |
|---|---|
| 介绍站 | https://zhaohairuiour.github.io/ets100-cloud/ |
| 仓库 | https://github.com/zhaohairuiour/ets100-cloud |
| applicationId | `com.shuaiqiu.fuckets100` |
| versionName | `2.3.0.3-fix` |
| versionCode | `27` |
| APK | [ets100-cloud-2.3.0.3-fix.apk](https://github.com/zhaohairuiour/ets100-cloud/releases/download/v2.3.0.3-fix/ets100-cloud-2.3.0.3-fix.apk) |
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

站点源码位于 `docs/`，纯静态、无构建步骤，由 Pages 的 **Deploy from a branch** 直接发布 `main` 分支的 `/docs` 目录，不依赖 Actions workflow。

```bash
# 本地预览
python3 -m http.server 8899 --directory docs
# 打开 http://127.0.0.1:8899/
```

仓库名为 `ets100-cloud` 时，Pages 地址为 `https://zhaohairuiour.github.io/ets100-cloud/`。首次部署在仓库 `Settings → Pages → Build and deployment → Source` 选择 **Deploy from a branch**，分支 `main`、目录 `/docs`。

设计语言是**深色科技风**：网格底纹加青 / 紫双色光晕，圆角玻璃卡片，阶梯胶囊按钮与编号章节，数据行统一等宽字体；默认深色主题，可切换到冷白浅色（同一套圆角与光效语言）。主题偏好键为 `fe-cloud-theme-v2`。滚动时各段元素逐个淡入上移（IntersectionObserver，同组 50ms 步进、上限 8 项），首屏封面元素在加载后依次入场。

页面为封面加十节：封面（版本元信息表）→ 摘要（滚动计数）→ 变更台账（八条可展开）→ 删与留（左列进入视口划删除线）→ 模块树（点击节点联动说明区）→ 入场时间轨（六条轨道，可播放 / 暂停 / 1×2×4× / 拖动滑块）→ 构建与安装（三段命令可复制）→ 版本与清单（真实请求 `version.json`，同源 → ghproxy.net → gh-proxy.com → raw 回退，JSON 着色 + versionCode 比对）→ 问答（五条折叠）→ 来源与致谢（解析器内核参考 LastStudio Fuck-ETS）→ 免责声明（责任边界与第三方关系）。

左侧常驻目录随滚动高亮，底部显示阅读百分比；窄屏（≤860px）收起侧栏，改为吸顶栏 + 主题切换。文件：`index.html`、`assets/style.css`、`assets/main.js`、`404.html`、`manifest.webmanifest`、`version.json` 与 192/512 图标。

## 版本清单

根目录 `version.json` 供 App 与站点共同消费；`docs/version.json` 是站点侧同源副本，两者需保持一致（`apkUrl` 在发布 Release 后填入）。

## 免责声明

本仓库及其构建产物为第三方个人改版，与 ETS100（e听说）及其运营方、学校、畅言、LastStudio 及任何第三方平台均无隶属、代理、授权或背书关系。

- 按现状提供，不保证可用性、准确性、完整性与持续可用
- 因下载、安装、登录或使用产生的一切后果与责任（账号受限或封禁、数据丢失、成绩与学术处分、考试违规、法律纠纷、设备或网络损失）由使用者自行承担
- 本仓库作者、贡献者及上述第三方不承担任何责任
- 仅限个人学习与技术交流；遵守学校、平台规则与当地法律，严禁考试作弊、代答或传播答案

应用内展示位置：引导页同意文案、登录页提示卡、关于页、用户协议页；站点见第 10 节「免责声明」。

## 来源

云端解析链路的内核实现参考了 **LastStudio 的 Fuck-ETS**：native 层的 `libresolver.so` / `libcloud.so`、试卷模板合并与答案解析的调用约定均源自该项目，本次改动只做模块删减与动画重写。账号与云端接口走 ETS100 官方 API，本仓库不托管任何服务端。

## 许可

本项目为他人项目的本地改造版本，原始项目声明源码闭源并禁止重新打包分发。**请勿将此仓库源码或构建产物公开分发**，仅作个人学习与备份使用；如需二次分发，需自行取得原始作者授权。
