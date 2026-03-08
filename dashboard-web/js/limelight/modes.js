"use strict";

// ==========================
// STATE
// ==========================
const state = {
  lime4:     { yaw: 0 },
  lime2:     { yaw: 0, alignPiece: 0 },
  lime2plus: { yaw: 0, shooter: 0 }
};

// ==========================
// NT TOPICS
// ==========================
const TOPIC_AIMLOCK_LIME4     = "/Modes/AimLockLime4";
const TOPIC_AIMLOCK_LIME2     = "/Modes/AimLockLime2";
const TOPIC_AIMLOCK_LIME2PLUS = "/Modes/AimLockLime2Plus";
const TOPIC_SHOOTER_LIME2PLUS = "/Modes/ShooterLime2Plus";
const TOPIC_ALIGN_PIECE       = "/Modes/AlignPiece";
const TOPIC_HW_LIME4          = "/limelight-front/hw";
const TOPIC_HW_LIME2          = "/limelight-back/hw";
const TOPIC_HW_LIME2PLUS      = "/limelight-lime2plus/hw";

// ==========================
// HELPERS
// ==========================
const TEMP_WARN_C = 70;

function clampInt(v, min, max) {
  const n = Math.trunc(Number(v));
  return Number.isFinite(n) ? Math.min(max, Math.max(min, n)) : min;
}

function renderMode(id, value) {
  const container = document.getElementById(id);
  if (!container) return;
  const box   = container.querySelector(".offset-box");
  const modes = container.querySelectorAll(".offset-modes span");
  const color = value === 1 ? "#00ff88" : value === 2 ? "#ffaa00" : "#444";
  box.style.background = color;
  modes.forEach(m => { m.style.color = "#888"; m.style.fontWeight = "normal"; });
  const active = container.querySelector(`.offset-modes span[data-mode="${value}"]`);
  if (active) { active.style.color = color; active.style.fontWeight = "bold"; }
}

function renderAll() {
  renderMode("yaw-lime4",         state.lime4.yaw);
  renderMode("yaw-lime2",         state.lime2.yaw);
  renderMode("align-piece",       state.lime2.alignPiece);
  renderMode("yaw-lime2plus",     state.lime2plus.yaw);
  renderMode("shooter-lime2plus", state.lime2plus.shooter);
}

function setTemp(elementId, tempC) {
  const el = document.getElementById(elementId);
  if (!el) return;
  const t = Number(tempC);
  if (!Number.isFinite(t)) {
    el.textContent = "Temp: --°C";
    el.classList.remove("warn");
    return;
  }
  const warn = t >= TEMP_WARN_C;
  el.textContent = `Temp: ${t.toFixed(0)}°C${warn ? " ⚠️" : ""}`;
  el.classList.toggle("warn", warn);
}

function extractTempFromHw(hwArr) {
  if (!Array.isArray(hwArr)) return null;
  const temp = Number(hwArr[0]);
  return Number.isFinite(temp) ? temp : null;
}

// ==========================
// WEBSOCKET
// ==========================
const WS_URL = "ws://127.0.0.1:5900/nt/dashboard";

function startWS() {
  let ws;
  let retryMs = 250;

  const connect = () => {
    ws = new WebSocket(WS_URL);

    ws.onopen = () => {
      console.log("[modes] WS conectado:", WS_URL);
      retryMs = 250;
    };

    ws.onmessage = (ev) => {
      let msg;
      try { msg = JSON.parse(ev.data); } catch { return; }
      if (!msg || typeof msg.topic !== "string") return;

      const topic = msg.topic;
      const value = msg.value;
      if (value === null || value === undefined) return;

      // -------------------------
      // MODOS
      // -------------------------
      if (topic === TOPIC_AIMLOCK_LIME4) {
        state.lime4.yaw = clampInt(value, 0, 1);
        renderMode("yaw-lime4", state.lime4.yaw);
        return;
      }

      if (topic === TOPIC_AIMLOCK_LIME2) {
        state.lime2.yaw = clampInt(value, 0, 1);
        renderMode("yaw-lime2", state.lime2.yaw);
        return;
      }

      if (topic === TOPIC_AIMLOCK_LIME2PLUS) {
        state.lime2plus.yaw = clampInt(value, 0, 1);
        renderMode("yaw-lime2plus", state.lime2plus.yaw);
        return;
      }

      if (topic === TOPIC_SHOOTER_LIME2PLUS) {
        state.lime2plus.shooter = clampInt(value, 0, 1);
        renderMode("shooter-lime2plus", state.lime2plus.shooter);
        return;
      }

      if (topic === TOPIC_ALIGN_PIECE) {
        state.lime2.alignPiece = clampInt(value, 0, 1);
        renderMode("align-piece", state.lime2.alignPiece);
        return;
      }

      // -------------------------
      // TEMPERATURA
      // -------------------------
      if (topic === TOPIC_HW_LIME4) {
        setTemp("temp-lime4", extractTempFromHw(value));
        return;
      }

      if (topic === TOPIC_HW_LIME2) {
        setTemp("temp-lime2", extractTempFromHw(value));
        return;
      }

      if (topic === TOPIC_HW_LIME2PLUS) {
        setTemp("temp-lime2plus", extractTempFromHw(value));
        return;
      }
    };

    ws.onclose = () => {
      console.warn("[modes] WS caiu. Reconectando...");
      setTimeout(connect, retryMs);
      retryMs = Math.min(retryMs * 2, 3000);
    };

    ws.onerror = () => {
      try { ws.close(); } catch {}
    };
  };

  connect();
}

// ==========================
// INIT
// ==========================
renderAll();
setTemp("temp-lime4", null);
setTemp("temp-lime2plus", null);
startWS();