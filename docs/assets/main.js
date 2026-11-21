/* ============================================================
   Fe Cloud — 改版档案
   交互层：主题 / 目录联动 / 阅读进度 / 计数 / 台账展开 /
   删除线入场 / 模块树联动 / 时间轨播放与拖拽 / 复制 /
   版本清单多源校验 / 问答折叠 / 轻提示
   ============================================================ */

(() => {
  "use strict";

  const $ = (s, r = document) => r.querySelector(s);
  const $$ = (s, r = document) => Array.from(r.querySelectorAll(s));
  const raf = requestAnimationFrame;
  const clamp = (v, a, b) => Math.min(b, Math.max(a, v));

  /* ---------------- 主题 ---------------- */

  const KEY = "fe-cloud-theme";
  const META = $('meta[name="theme-color"]');

  function applyTheme(t) {
    document.documentElement.setAttribute("data-theme", t);
    const dark = t === "dark";
    $$("#themeLabel").forEach((el) => (el.textContent = dark ? "Light" : "Dark"));
    $$("#themeToggleMobile").forEach((el) => (el.textContent = dark ? "Light" : "Dark"));
    if (META) META.setAttribute("content", dark ? "#13100d" : "#f3efe6");
    try { localStorage.setItem(KEY, t); } catch (e) {}
  }

  function toggleTheme() {
    const cur = document.documentElement.getAttribute("data-theme") === "dark" ? "dark" : "light";
    applyTheme(cur === "dark" ? "light" : "dark");
  }

  function initTheme() {
    let saved = null;
    try { saved = localStorage.getItem(KEY); } catch (e) {}
    applyTheme(saved === "dark" ? "dark" : "light");
    $("#themeToggle")?.addEventListener("click", toggleTheme);
    $("#themeToggleMobile")?.addEventListener("click", toggleTheme);
  }

  /* ---------------- 提示 ---------------- */

  let toastTimer = 0;
  function toast(msg) {
    const el = $("#toast");
    if (!el) return;
    el.textContent = msg;
    el.classList.add("is-show");
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => el.classList.remove("is-show"), 1900);
  }

  /* ---------------- 阅读进度 + 目录联动 ---------------- */

  function initReading() {
    const fill = $("#readFill");
    const pct = $("#readPct");
    const items = $$(".toc-item");
    const sections = items
      .map((a) => {
        const id = (a.getAttribute("href") || "").replace("#", "");
        return id ? { a, el: document.getElementById(id) } : null;
      })
      .filter(Boolean);

    const onScroll = () => {
      const y = window.scrollY || 0;
      const max = Math.max(1, document.documentElement.scrollHeight - window.innerHeight);
      const p = clamp(y / max, 0, 1);
      if (fill) fill.style.width = (p * 100).toFixed(2) + "%";
      if (pct) pct.textContent = Math.round(p * 100) + "%";

      let current = sections[0];
      sections.forEach((s) => {
        if (!s.el) return;
        if (s.el.getBoundingClientRect().top <= 140) current = s;
      });
      items.forEach((a) => a.classList.remove("is-current"));
      current?.a.classList.add("is-current");
    };

    window.addEventListener("scroll", onScroll, { passive: true });
    window.addEventListener("resize", onScroll);
    onScroll();
  }

  /* ---------------- 计数 ---------------- */

  function initCounters() {
    const nums = $$(".fig-num");
    if (!nums.length) return;

    const run = (el) => {
      const target = Number(el.dataset.count || 0);
      if (!target) { el.textContent = "0"; return; }
      const t0 = performance.now();
      const dur = 780;
      const step = (t) => {
        const k = clamp((t - t0) / dur, 0, 1);
        const e = 1 - Math.pow(1 - k, 3);
        el.textContent = String(Math.round(target * e));
        if (k < 1) raf(step);
      };
      raf(step);
    };

    const io = new IntersectionObserver(
      (es) => es.forEach((e) => {
        if (e.isIntersecting) { run(e.target); io.unobserve(e.target); }
      }),
      { threshold: 0.5 }
    );
    nums.forEach((n) => io.observe(n));
  }

  function initLedger() {
    $$(".entry").forEach((entry) => {
      const head = $(".entry-head", entry);
      const body = $(".entry-body", entry);
      if (!head || !body) return;
      const open = () => entry.classList.contains("is-open");

      head.addEventListener("click", () => {
        const next = !open();
        head.setAttribute("aria-expanded", String(next));
        entry.classList.toggle("is-open", next);
        if (next) {
          body.style.height = body.scrollHeight + "px";
        } else {
          body.style.height = body.scrollHeight + "px";
          void body.offsetHeight;
          body.style.height = "0px";
        }
      });
    });

    window.addEventListener("resize", () => {
      $$(".entry").forEach((entry) => {
        if (!entry.classList.contains("is-open")) return;
        const body = $(".entry-body", entry);
        if (body) body.style.height = body.querySelector(".entry-body-inner")?.scrollHeight + 2 + "px";
      });
    });
  }

  /* ---------------- 删除线入场 ---------------- */

  function initStrike() {
    const rows = $$(".cut-row[data-strike]");
    if (!rows.length) return;
    const io = new IntersectionObserver(
      (es) => es.forEach((e) => {
        if (e.isIntersecting) {
          e.target.classList.add("is-hit");
          io.unobserve(e.target);
        }
      }),
      { threshold: 0.6 }
    );
    rows.forEach((r, i) => {
      r.style.setProperty("--strike-delay", (i * 60) + "ms");
      io.observe(r);
    });
  }

  /* ---------------- 模块树 ---------------- */

  const MODULES = {
    app: {
      t: "app",
      d: "应用入口与主题层。承载引导与主界面的切换、导航容器、畅言登录 Activity、Hilt 装配。",
      l: [
        "FeAppMain · 引导 / 主界面切换，主界面 alpha 淡入",
        "FeNavContainer · NavDisplay + 预测性返回动画",
        "ChangyanWebLoginActivity · 网页登录备用入口",
        "AppModule / AppContainer · Hilt Provider",
      ],
    },
    core: {
      t: "core/*",
      d: "六个基础模块，提供模型、设计系统、设置存储、平台能力与导航基建。",
      l: [
        "core:model · ActivationMode 等枚举",
        "core:designsystem · ExpressiveScaffold、TonalCard",
        "core:settings · SettingsManager / ThemeManager",
        "core:navigation · PredictiveBackAnimationHandler",
      ],
    },
    data: {
      t: "data/*",
      d: "数据层，仅两个子模块：content 负责远端配置与协议，ets100 负责账号与云端 API。",
      l: [
        "data:content · RemoteConfigManager、AppUpdateStatusRepository",
        "data:ets100 · ETS100ApiClient、ETS100AuthManager",
        "data:account 已整体移除",
      ],
    },
    content: {
      t: "data:content",
      d: "远端配置、公告、协议与更新状态的读取层，失败时回退到内置默认值。",
      l: [
        "RemoteConfigManager · 配置拉取与校验码比对",
        "LegalAgreementRepository · 用户协议与隐私政策",
        "AppUpdateStatusRepository · 版本状态",
      ],
    },
    ets100: {
      t: "data:ets100",
      d: "云端链路核心：登录、设备绑定、作业列表、下载、模板合并与答案解析的数据层能力。",
      l: [
        "ETS100AuthManager · 手机号 / 密码、token、deviceCode",
        "ETS100ApiClient · 作业接口与异常类型",
        "CloudMerge / CloudTemplate · 试卷模板合并",
      ],
    },
    "removed-account": {
      t: "data:account（已移除）",
      d: "第三方账户体系的本地数据层，随账户入口一并删除，不再参与构建。",
      l: ["账户登录与绑定", "云端授权与头像", "验证码入口"],
    },
    feature: {
      t: "feature/*",
      d: "界面层，六个模块：激活、引导、首页、阅读、设置、共享组件。",
      l: [
        "activation · 云端激活与设备验证",
        "onboarding · 三步引导与阶梯入场",
        "read · 四态阅读与列表入场",
      ],
    },
    activation: {
      t: "feature:activation",
      d: "运行授权。三种本地模式面板已删除，只保留云端登录与授权状态。",
      l: [
        "ActivationViewModel · 仅维护云端登录状态",
        "CloudActivationScreen · 手机号 + 密码表单",
        "DeviceVerificationDialog · 设备绑定短信验证",
      ],
    },
    onboarding: {
      t: "feature:onboarding",
      d: "三步引导：欢迎 → 运行模式（仅云端）→ 完成。全部 draw-phase 动画。",
      l: [
        "WelcomeStep · spring 缩放 + 阶梯淡入",
        "ModeAndPermissionStep · 三段 stagger",
        "OnboardingStepIndicator · 连续进度插值",
      ],
    },
    home: {
      t: "feature:home",
      d: "首页与作业列表。本地模式状态条已移除，只展示云端作业。",
      l: [
        "HomeViewModel · 仅云端数据源",
        "HomeScreen · 作业卡片与刷新",
      ],
    },
    read: {
      t: "feature:read",
      d: "阅读页。已重写为纯云端委托，本地加载链整体移除。",
      l: [
        "ReadViewModel · 仅注入 ApplicationContext",
        "ReadScreen · gate / loading / error / list 四态",
        "StaggerEntrance · 按 index 阶梯入场",
      ],
    },
    settings: {
      t: "feature:settings",
      d: "设置与关于。账户区块删除，保留主题、缓存、协议与版本信息。",
      l: ["SettingsScreen · 主题与缓存", "AboutScreen · 版本与来源说明"],
    },
    shared: {
      t: "feature:shared",
      d: "跨模块共享的 UI 组件与工具。",
      l: ["通用卡片、空态、加载指示", "格式化与扩展函数"],
    },
    "removed-account-ui": {
      t: "feature:account（已移除）",
      d: "账户界面层，包含登录、个人中心、绑定与头像，整模块删除。",
      l: ["账户登录页", "个人中心", "绑定与头像"],
    },
    baseline: {
      t: "baselineprofile",
      d: "启动路径的 Baseline Profile 模块，用于优化首帧与滚动性能。",
      l: ["启动与滚动关键路径采样", "随 release 构建生成 profile"],
    },
    docs: {
      t: "docs",
      d: "本页所在目录。纯静态站点，由 Pages 工作流在 main 分支变更时发布。",
      l: ["index.html · 本档案", "assets/ · 样式与交互", "version.json · 站点侧清单副本"],
    },
    scripts: {
      t: "scripts",
      d: "仓库脚本：构建校验与模块边界检查，保留不依赖外部服务的部分。",
      l: ["architecture/check_module_boundaries.py", "release/read_version.py", "release/check_release_ready.py"],
    },
  };

  function initTree() {
    const title = $("#treeReadTitle");
    const desc = $("#treeReadDesc");
    const list = $("#treeReadList");
    if (!title || !desc || !list) return;

    const render = (key) => {
      const m = MODULES[key];
      if (!m) return;
      title.textContent = m.t;
      desc.textContent = m.d;
      list.innerHTML = m.l.map((x) => `<li>${x}</li>`).join("");
      $$(".tree-line[data-mod]").forEach((l) =>
        l.classList.toggle("is-active", l.dataset.mod === key)
      );
    };

    $$(".tree-line[data-mod]").forEach((line) => {
      line.setAttribute("tabindex", "0");
      line.setAttribute("role", "button");
      line.addEventListener("click", () => render(line.dataset.mod));
      line.addEventListener("keydown", (e) => {
        if (e.key === "Enter" || e.key === " ") {
          e.preventDefault();
          render(line.dataset.mod);
        }
      });
    });

    render("app");
  }

  /* ---------------- 时间轨 ---------------- */

  const TOTAL = 1000;

  function initTimeline() {
    const rows = $$("#tracks .track-row");
    if (!rows.length) return;

    const scrub = $("#scrub");
    const clock = $("#clock");
    const playBtn = $("#playBtn");

    // 轨道条与播放头按比例定位
    rows.forEach((row) => {
      const delay = Number(row.dataset.delay || 0);
      const dur = Number(row.dataset.dur || 0);
      const bar = $(".track-bar", row);
      const head = $(".playhead", row);
      if (bar) {
        bar.style.left = (delay / TOTAL) * 100 + "%";
        bar.style.width = Math.max(0.6, (dur / TOTAL) * 100) + "%";
      }
    });

    let t = 0;
    let speed = 1;
    let rafId = 0;
    let playing = false;
    let last = 0;

    const paint = (value) => {
      t = clamp(value, 0, TOTAL);
      rows.forEach((row) => {
        const delay = Number(row.dataset.delay || 0);
        const dur = Number(row.dataset.dur || 0);
        const head = $(".playhead", row);
        if (head) {
          head.style.left = (t / TOTAL) * 100 + "%";
          head.style.opacity = "1";
        }
        const live = t >= delay && t <= delay + dur;
        row.classList.toggle("is-live", live);
      });
      if (scrub) scrub.value = String(Math.round(t));
      if (clock) clock.textContent = String(Math.round(t)).padStart(4, "0");
    };

    const stop = () => {
      playing = false;
      cancelAnimationFrame(rafId);
      if (playBtn) playBtn.textContent = "▶ 播放";
    };

    const loop = (now) => {
      if (!playing) return;
      if (!last) last = now;
      const dt = Math.min(80, now - last);
      last = now;
      t += dt * speed;
      if (t >= TOTAL) {
        paint(TOTAL);
        stop();
        return;
      }
      paint(t);
      rafId = raf(loop);
    };

    const play = () => {
      if (playing) { stop(); return; }
      if (t >= TOTAL) t = 0;
      playing = true;
      last = 0;
      if (playBtn) playBtn.textContent = "‖ 暂停";
      rafId = raf(loop);
    };

    playBtn?.addEventListener("click", play);

    $$("[data-speed]").forEach((btn) => {
      btn.addEventListener("click", () => {
        speed = Number(btn.dataset.speed) || 1;
        $$("[data-speed]").forEach((b) => b.classList.toggle("is-on", b === btn));
        if (playing) { t = Math.min(t, TOTAL); }
      });
    });

    scrub?.addEventListener("input", () => {
      stop();
      paint(Number(scrub.value));
    });

    paint(0);

    // 首次进入视口时自动播放一次
    const tracks = $("#tracks");
    if (tracks) {
      const io = new IntersectionObserver(
        (es) => es.forEach((e) => {
          if (e.isIntersecting) {
            io.unobserve(e.target);
            setTimeout(play, 260);
          }
        }),
        { threshold: 0.45 }
      );
      io.observe(tracks);
    }
  }

  /* ---------------- 复制 ---------------- */

  function initCopy() {
    $$(".copy").forEach((btn) => {
      btn.addEventListener("click", async () => {
        const id = btn.dataset.copy;
        const el = id ? document.getElementById(id) : null;
        if (!el) return;
        const text = (el.textContent || "").replace(/\u00a0/g, " ").trim();
        try {
          await navigator.clipboard.writeText(text);
          toast("已复制");
        } catch (e) {
          const ta = document.createElement("textarea");
          ta.value = text;
          ta.style.position = "fixed";
          ta.style.opacity = "0";
          document.body.appendChild(ta);
          ta.select();
          try { document.execCommand("copy"); toast("已复制"); }
          catch (err) { toast("复制失败"); }
          document.body.removeChild(ta);
        }
      });
    });
  }

  /* ---------------- 版本清单 ---------------- */

  const REPO = "zhaohairuiour/ets100-cloud";
  const BRANCH = "main";

  const esc = (s) => s.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");

  function colorJson(raw) {
    return esc(raw)
      .replace(/(&quot;|")([^"\\]*(?:\\.[^"\\]*)*)(&quot;|")(\s*:)/g,
        (_m, q1, k, q2, colon) => `<span class="k">"${k}"</span>${colon}`)
      .replace(/:(\s*)(&quot;|")([^"\\]*(?:\\.[^"\\]*)*)(&quot;|")/g,
        (_m, sp, q1, v, q2) => `:${sp}<span class="s">"${v}"</span>`)
      .replace(/:(\s*)(-?\d+(\.\d+)?)/g, (_m, sp, n) => `:${sp}<span class="n">${n}</span>`)
      .replace(/:(\s*)(true|false|null)/g, (_m, sp, v) => `:${sp}<span class="n">${v}</span>`);
  }

  function initRelease() {
    const out = $("#consoleOut");
    const btn = $("#probeBtn");
    const force = $("#probeForce");
    if (!out || !btn) return;

    const sources = (cacheBust) => {
      const q = cacheBust ? "?t=" + Date.now() : "";
      return [
        { label: "本站 version.json", url: "version.json" + q, same: true },
        { label: "ghproxy.net", url: `https://ghproxy.net/https://raw.githubusercontent.com/${REPO}/${BRANCH}/version.json` },
        { label: "gh-proxy.com", url: `https://gh-proxy.com/https://raw.githubusercontent.com/${REPO}/${BRANCH}/version.json` },
        { label: "raw.githubusercontent", url: `https://raw.githubusercontent.com/${REPO}/${BRANCH}/version.json` },
      ];
    };

    let busy = false;

    const probe = async (cacheBust) => {
      if (busy) return;
      busy = true;
      out.hidden = false;
      out.textContent = "请求中…";

      const local = Number($("#verCode")?.textContent || 0);
      const t0 = performance.now();
      const log = [];

      for (const src of sources(cacheBust)) {
        try {
          const res = await fetch(src.url, { cache: "no-store" });
          if (!res.ok) {
            log.push(`✗ ${src.label} · HTTP ${res.status}`);
            continue;
          }
          const data = await res.json();
          const ms = Math.round(performance.now() - t0);
          const remote = Number(data.versionCode || 0);
          const verdict = remote > local ? "有新版本" : remote === local ? "已是最新" : "本地更新";
          out.innerHTML =
            `<div>✓ ${src.label} · ${ms}ms · <span class="k">${verdict}</span>（远端 ${remote} / 本地 ${local}）</div>` +
            `<div style="height:1px;background:var(--rule);margin:10px 0"></div>` +
            `<div>${colorJson(JSON.stringify(data, null, 2))}</div>`;

          if (data.versionName) $("#tblName") && ($("#tblName").textContent = data.versionName);
          if (data.versionCode != null) $("#tblCode") && ($("#tblCode").textContent = String(data.versionCode));

          toast("清单已拉到");
          busy = false;
          return;
        } catch (e) {
          log.push(`✗ ${src.label} · 不可达`);
        }
      }

      out.innerHTML = `<div>四个来源均失败：</div><div>${log.join("\n")}</div>`;
      toast("拉取失败");
      busy = false;
    };

    btn.addEventListener("click", () => probe(false));
    force?.addEventListener("click", () => probe(true));
  }

  /* ---------------- 问答 ---------------- */

  function initQa() {
    $$(".qa-item").forEach((item) => {
      const q = $(".qa-q", item);
      const a = $(".qa-a", item);
      if (!q || !a) return;
      q.addEventListener("click", () => {
        const next = !item.classList.contains("is-open");
        item.classList.toggle("is-open", next);
        q.setAttribute("aria-expanded", String(next));
        if (next) a.style.height = a.scrollHeight + "px";
        else {
          a.style.height = a.scrollHeight + "px";
          void a.offsetHeight;
          a.style.height = "0px";
        }
      });
    });
  }

  /* ---------------- 首屏版本 ---------------- */

  function initVersionPill() {
    fetch("version.json", { cache: "no-store" })
      .then((r) => (r.ok ? r.json() : null))
      .then((d) => {
        if (!d) return;
        const name = d.versionName ? "v" + String(d.versionName).replace(/^v/, "") : null;
        ["#verName", "#verName2"].forEach((s) => {
          const el = $(s);
          if (el && name) el.textContent = name;
        });
        if (d.versionCode != null) {
          const el = $("#verCode");
          if (el) el.textContent = String(d.versionCode);
        }
      })
      .catch(() => {});
  }

  /* ---------------- 入场 ---------------- */

  function initReveal() {
    const targets = $$(".section-head, .figures, .entry, .cut-col, .tree, .tree-read, .ver-table, .qa-item");
    if (!targets.length) return;
    const io = new IntersectionObserver(
      (es) => es.forEach((e) => {
        if (e.isIntersecting) {
          e.target.classList.add("is-in");
          io.unobserve(e.target);
        }
      }),
      { threshold: 0.08 }
    );
    targets.forEach((tg) => { tg.classList.add("reveal"); io.observe(tg); });
  }

  /* ---------------- 启动 ---------------- */

  function boot() {
    initTheme();
    initReading();
    initCounters();
    initLedger();
    initStrike();
    initTree();
    initTimeline();
    initCopy();
    initRelease();
    initQa();
    initVersionPill();
    initReveal();
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", boot);
  } else {
    boot();
  }
})();
