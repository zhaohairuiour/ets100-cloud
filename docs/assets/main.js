/* ============================================================
   Fe Cloud · site script
   theme / scroll progress / typer / counters / tilt / spotlight
   tabs / architecture / motion replay / copy / version check / faq
   ============================================================ */

(() => {
  "use strict";

  const $ = (sel, root = document) => root.querySelector(sel);
  const $$ = (sel, root = document) => Array.from(root.querySelectorAll(sel));
  const raf = requestAnimationFrame;

  /* ---------------- theme ---------------- */

  const THEME_KEY = "fe-cloud-theme";

  function initTheme() {
    let saved = null;
    try { saved = localStorage.getItem(THEME_KEY); } catch { /* ignore */ }
    const prefersLight = window.matchMedia("(prefers-color-scheme: light)").matches;
    const theme = saved || (prefersLight ? "light" : "dark");
    applyTheme(theme);
  }

  function applyTheme(theme) {
    document.documentElement.setAttribute("data-theme", theme);
    const meta = $('meta[name="theme-color"]');
    if (meta) meta.setAttribute("content", theme === "light" ? "#f6f8fb" : "#0b0f14");
    try { localStorage.setItem(THEME_KEY, theme); } catch { /* ignore */ }
  }

  function toggleTheme() {
    const cur = document.documentElement.getAttribute("data-theme");
    applyTheme(cur === "light" ? "dark" : "light");
    toast(cur === "light" ? "已切换到深色主题" : "已切换到浅色主题");
  }

  /* ---------------- toast ---------------- */

  let toastTimer = 0;
  function toast(message) {
    const el = $("#toast");
    if (!el) return;
    el.textContent = message;
    el.classList.add("is-show");
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => el.classList.remove("is-show"), 1800);
  }

  /* ---------------- nav ---------------- */

  function initNav() {
    const nav = $("#nav");
    const burger = $("#navBurger");

    const onScroll = () => {
      const y = window.scrollY || 0;
      nav.classList.toggle("is-scrolled", y > 8);
      const max = Math.max(1, document.documentElement.scrollHeight - window.innerHeight);
      const p = Math.min(1, y / max);
      $("#scrollProgress").style.width = (p * 100).toFixed(2) + "%";
    };

    window.addEventListener("scroll", onScroll, { passive: true });
    onScroll();

    if (burger) {
      burger.addEventListener("click", () => {
        const open = nav.classList.toggle("is-open");
        burger.setAttribute("aria-expanded", String(open));
      });
    }

    $$(".nav-links a").forEach((a) =>
      a.addEventListener("click", () => nav.classList.remove("is-open"))
    );
  }

  /* ---------------- hero typer ---------------- */

  function initTyper() {
    const el = $("#typer");
    if (!el) return;
    const lines = [
      "只读云端，不做多余的事",
      "Shizuku / Root / 直读：已下线",
      "Animatable + graphicsLayer：零重组动画",
      "2.3.0.1-fix · versionCode 25",
    ];
    let li = 0;
    let ci = 0;
    let deleting = false;

    const tick = () => {
      const text = lines[li];
      if (!deleting) {
        ci++;
        el.textContent = text.slice(0, ci);
        if (ci >= text.length) {
          deleting = true;
          setTimeout(tick, 1500);
          return;
        }
        setTimeout(tick, 55);
      } else {
        ci--;
        el.textContent = text.slice(0, ci);
        if (ci <= 0) {
          deleting = false;
          li = (li + 1) % lines.length;
          setTimeout(tick, 260);
          return;
        }
        setTimeout(tick, 26);
      }
    };
    tick();
  }

  /* ---------------- counters ---------------- */

  function initCounters() {
    const nums = $$(".stat-num");
    if (!nums.length) return;

    const animate = (el) => {
      const target = Number(el.dataset.count || 0);
      const dur = 900;
      const t0 = performance.now();
      const step = (t) => {
        const p = Math.min(1, (t - t0) / dur);
        const eased = 1 - Math.pow(1 - p, 3);
        el.textContent = String(Math.round(target * eased));
        if (p < 1) raf(step);
      };
      raf(step);
    };

    const io = new IntersectionObserver(
      (entries) => {
        entries.forEach((e) => {
          if (e.isIntersecting) {
            animate(e.target);
            io.unobserve(e.target);
          }
        });
      },
      { threshold: 0.4 }
    );
    nums.forEach((n) => io.observe(n));
  }

  /* ---------------- tilt ---------------- */

  function initTilt() {
    const device = $(".device");
    if (!device) return;

    const hero = $("#hero");
    if (!hero) return;

    hero.addEventListener("pointermove", (e) => {
      const rect = hero.getBoundingClientRect();
      const nx = (e.clientX - rect.left) / rect.width - 0.5;
      const ny = (e.clientY - rect.top) / rect.height - 0.5;
      const rx = (-ny * 10).toFixed(2);
      const ry = (nx * 14).toFixed(2);
      device.style.transform = `rotateY(${ry}deg) rotateX(${rx}deg)`;
    });

    hero.addEventListener("pointerleave", () => {
      device.style.transform = "";
    });
  }

  /* ---------------- spotlight ---------------- */

  function initSpotlight() {
    $$("[data-spot]").forEach((card) => {
      card.addEventListener("pointermove", (e) => {
        const r = card.getBoundingClientRect();
        card.style.setProperty("--mx", ((e.clientX - r.left) / r.width) * 100 + "%");
        card.style.setProperty("--my", ((e.clientY - r.top) / r.height) * 100 + "%");
      });
    });
  }

  /* ---------------- tabs ---------------- */

  function initTabs() {
    const tabs = $$(".tab");
    const panels = $$(".tab-panel");
    if (!tabs.length) return;

    tabs.forEach((tab) => {
      tab.addEventListener("click", () => {
        const key = tab.dataset.tab;
        tabs.forEach((t) => {
          const on = t === tab;
          t.classList.toggle("is-active", on);
          t.setAttribute("aria-selected", String(on));
        });
        panels.forEach((p) => p.classList.toggle("is-active", p.dataset.panel === key));
      });
    });
  }

  /* ---------------- architecture ---------------- */

  const ARCH = {
    app: {
      title: "app",
      desc: "应用入口与主题层。承载 MainActivity、FeAppMain、FeNavContainer、Changyan 登录 Activity、Hilt 容器与主题管理。",
      items: [
        "FeAppMain：引导/主界面切换与主界面 alpha 淡入",
        "FeNavContainer：NavDisplay + 预测性返回动画",
        "ChangyanWebLoginActivity：畅言网页登录备用入口",
        "AppModule / AppContainer：Hilt Provider 装配",
      ],
    },
    core: {
      title: "core/*",
      desc: "common / model / designsystem / settings / platform / navigation 六个基础模块，提供模型、设计系统、设置存储、平台能力与导航基建。",
      items: [
        "core:model：ActivationMode 等枚举",
        "core:designsystem：ExpressiveScaffold、TonalCard、LargeFlexibleTopAppBar",
        "core:settings：SettingsManager / ThemeManager",
        "core:navigation：PredictiveBackAnimationHandler 实现族",
      ],
    },
    data: {
      title: "data/*",
      desc: "content 与 ets100 两个数据模块。content 负责远程配置、公告、更新状态与协议；ets100 负责账号、云端 API 与试卷模型。",
      items: [
        "data:content：RemoteConfigManager、AppUpdateStatusRepository、LegalAgreementRepository",
        "data:ets100：ETS100ApiClient、ETS100AuthManager、ResolverAuthorizer",
        "已移除 data:account",
      ],
    },
    feature: {
      title: "feature/*",
      desc: "activation / onboarding / home / read / settings / shared 六个界面模块。",
      items: [
        "feature:activation：云端激活页、CloudActivationScreen",
        "feature:onboarding：三步引导与 stagger 入场",
        "feature:read：四态内容与列表 StaggerEntrance",
        "已移除 feature:account",
      ],
    },
    ets100: {
      title: "data:ets100",
      desc: "云端链路核心。包含登录、设备绑定、作业列表、下载、模板合并与答案解析所需的全部数据层能力。",
      items: [
        "ETS100AuthManager：手机号/密码登录、token、deviceCode",
        "ETS100ApiClient：作业接口与异常类型",
        "CloudMerge / CloudTemplate：试卷模板合并",
        "PaperSource(Cloud)：云端试卷来源模型",
      ],
    },
    read: {
      title: "feature:read",
      desc: "阅读页。已重写为纯云端委托，本地加载链整体移除。",
      items: [
        "ReadViewModel：仅注入 ApplicationContext",
        "ReadScreen：gate / loading / error / list 四态",
        "ReadCloudContent：Crossfade 列表与详情",
        "StaggerEntrance：按 index 阶梯入场",
      ],
    },
    onboarding: {
      title: "feature:onboarding",
      desc: "三步引导：欢迎 → 运行模式（仅云端）→ 完成。全部使用 draw-phase 动画。",
      items: [
        "WelcomeStep：spring 缩放 + 阶梯淡入",
        "ModeAndPermissionStep：三段 stagger",
        "FinishStep：图标弹性 + 摘要卡上滑",
        "OnboardingStepIndicator：连续进度条插值",
      ],
    },
    activation: {
      title: "feature:activation",
      desc: "运行授权。三种本地模式面板已删除，仅保留云端登录与授权状态。",
      items: [
        "ActivationViewModel：仅维护云端登录状态",
        "CloudActivationScreen：手机号 + 密码表单",
        "DeviceVerificationDialog：设备绑定短信验证",
        "已删除 Shizuku / Root / DirectRead 面板",
      ],
    },
  };

  function initArch() {
    const detail = $("#archDetail");
    if (!detail) return;
    const nodes = $$("[data-node]");

    const render = (key) => {
      const info = ARCH[key];
      if (!info) return;
      detail.innerHTML = `
        <h3>${info.title}</h3>
        <p>${info.desc}</p>
        <ul>${info.items.map((i) => `<li>${i}</li>`).join("")}</ul>
      `;
      nodes.forEach((n) => n.classList.toggle("is-active", n.dataset.node === key));
    };

    nodes.forEach((n) => {
      n.addEventListener("click", () => {
        if (n.disabled) return;
        render(n.dataset.node);
      });
    });

    render("app");
  }

  /* ---------------- motion replay ---------------- */

  let motionSpeed = 1;
  let motionTimers = [];

  function resetMotion() {
    motionTimers.forEach((t) => clearTimeout(t));
    motionTimers = [];
    $$(".m-node").forEach((n) => {
      n.classList.remove("is-in");
      n.style.animationDelay = "0ms";
      void n.offsetWidth;
    });
  }

  function playMotion() {
    resetMotion();
    const stage = $("#motionStage");
    if (!stage) return;
    const rows = $$(".m-row", stage);
    rows.forEach((r, i) => r.setAttribute("data-row", String(i)));

    const targets = [
      { el: $(".m-logo", stage), delay: 0 },
      { el: $(".m-title", stage), delay: 160 },
      { el: rows[0], delay: 340 },
      { el: rows[1], delay: 400 },
      { el: rows[2], delay: 460 },
      { el: $(".m-cta", stage), delay: 560 },
    ];

    targets.forEach(({ el, delay }) => {
      if (!el) return;
      const scaled = Math.round(delay / motionSpeed);
      const timer = setTimeout(() => el.classList.add("is-in"), scaled);
      motionTimers.push(timer);
    });
  }

  function initMotion() {
    const replay = $("#motionReplay");
    const stage = $("#motionStage");
    if (!replay || !stage) return;

    replay.addEventListener("click", playMotion);

    $$("#speedGroup .speed-btn").forEach((btn) => {
      btn.addEventListener("click", () => {
        $$("#speedGroup .speed-btn").forEach((b) => b.classList.remove("is-active"));
        btn.classList.add("is-active");
        motionSpeed = Number(btn.dataset.speed) || 1;
        playMotion();
      });
    });

    const io = new IntersectionObserver(
      (entries) => {
        entries.forEach((e) => {
          if (e.isIntersecting) {
            playMotion();
            io.unobserve(e.target);
          }
        });
      },
      { threshold: 0.35 }
    );
    io.observe(stage);
  }

  /* ---------------- copy ---------------- */

  function initCopy() {
    $$(".copy-btn").forEach((btn) => {
      btn.addEventListener("click", async () => {
        const target = btn.dataset.copy;
        const el = target ? $(target) : null;
        if (!el) return;
        const text = el.textContent || "";
        try {
          await navigator.clipboard.writeText(text.trim());
          toast("已复制到剪贴板");
        } catch {
          const ta = document.createElement("textarea");
          ta.value = text.trim();
          document.body.appendChild(ta);
          ta.select();
          try {
            document.execCommand("copy");
            toast("已复制到剪贴板");
          } catch {
            toast("复制失败，请手动选择");
          }
          document.body.removeChild(ta);
        }
      });
    });
  }

  /* ---------------- version check ---------------- */

  const MIRRORS = [
    "", // 同源直连
    "https://ghproxy.net/https://raw.githubusercontent.com/",
    "https://gh-proxy.com/https://raw.githubusercontent.com/",
  ];

  const REPO = "zhaohairuiour/ets100-cloud";
  const BRANCH = "main";

  function highlightJson(obj) {
    const raw = JSON.stringify(obj, null, 2);
    return raw
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/"/g, "&quot;")
      .replace(/(&quot;[^&]*?&quot;)(\s*:)/g, '<span class="k">$1</span>$2')
      .replace(/(: )(&quot;[^&]*?&quot;)/g, '$1<span class="s">$2</span>')
      .replace(/(: )(\d+)/g, '$1<span class="n">$2</span>');
  }

  async function fetchVersion() {
    const out = $("#versionOut");
    if (!out) return;
    out.hidden = false;
    out.textContent = "请求中…";

    const t0 = performance.now();
    const sources = [
      "version.json",
      `${MIRRORS[1]}${REPO}/${BRANCH}/version.json`,
      `${MIRRORS[2]}${REPO}/${BRANCH}/version.json`,
      `https://raw.githubusercontent.com/${REPO}/${BRANCH}/version.json`,
    ];

    for (const src of sources) {
      try {
        const res = await fetch(src, { cache: "no-store" });
        if (!res.ok) continue;
        const data = await res.json();
        const ms = Math.round(performance.now() - t0);
        const local = Number(
          ($("#versionCode") && $("#versionCode").textContent) || 0
        );
        const remote = Number(data.versionCode || 0);
        const label = src.startsWith("http")
          ? new URL(src).host
          : "本站 version.json";
        out.innerHTML =
          `<div>来源：${label} · 耗时 ${ms}ms</div>` +
          `<div>远端 versionCode=${remote}，本地 versionCode=${local} → ` +
          (remote > local ? "有新版本" : "已是最新") +
          `</div><hr style="border:0;border-top:1px solid var(--line);margin:10px 0" />` +
          highlightJson(data);
        toast("版本清单拉取成功");
        return;
      } catch {
        /* 继续下一个源 */
      }
    }

    out.textContent = "全部来源不可达（同源 / ghproxy.net / gh-proxy.com / raw）";
    toast("版本清单拉取失败");
  }

  /* ---------------- reveal ---------------- */

  function initReveal() {
    const targets = $$(".card, .section-head, .build-card, .list-row, details");
    if (!targets.length) return;
    const io = new IntersectionObserver(
      (entries) => {
        entries.forEach((e) => {
          if (e.isIntersecting) {
            e.target.classList.add("is-in");
            io.unobserve(e.target);
          }
        });
      },
      { threshold: 0.12 }
    );
    targets.forEach((t) => {
      t.classList.add("reveal");
      io.observe(t);
    });
  }

  /* ---------------- boot ---------------- */

  function boot() {
    initTheme();
    initNav();
    initTyper();
    initCounters();
    initTilt();
    initSpotlight();
    initTabs();
    initArch();
    initMotion();
    initCopy();
    initReveal();

    const themeBtn = $("#themeToggle");
    if (themeBtn) themeBtn.addEventListener("click", toggleTheme);

    const check = $("#checkVersion");
    if (check) check.addEventListener("click", fetchVersion);

    // 首屏版本 pill
    const vt = $("#versionText");
    const vc = $("#versionCode");
    if (vt || vc) {
      fetch("version.json", { cache: "no-store" })
        .then((r) => (r.ok ? r.json() : null))
        .then((d) => {
          if (!d) return;
          if (vt && d.versionName) vt.textContent = "v" + d.versionName;
          if (vc && d.versionCode != null) vc.textContent = String(d.versionCode);
        })
        .catch(() => {});
    }
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", boot);
  } else {
    boot();
  }
})();
