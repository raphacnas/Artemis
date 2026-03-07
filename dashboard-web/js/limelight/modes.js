// ==========================
// ESTADO GLOBAL
// ==========================
const state = {
  lime4: { yaw: 0 },
  lime2: { yaw: 0, alignPiece: 0 },
  lime2plus: { yaw: 0, shooter: 0 }
};

// ==========================
// RENDERIZAÇÃO VISUAL (MODOS)
// ==========================
function renderMode(id, value) {
  const container = document.getElementById(id);
  if (!container) return;

  const box = container.querySelector(".offset-box");
  const modes = container.querySelectorAll(".offset-modes span");

  let color = "#444";
  switch (value) {
    case 0: color = "#444"; break;     // OFF
    case 1: color = "#00ff88"; break;  // ON / TAG
    case 2: color = "#ffaa00"; break;  // AUTO / BALL
  }

  box.style.background = color;

  modes.forEach(m => {
    m.style.color = "#888";
    m.style.fontWeight = "normal";
  });

  const active = container.querySelector(`.offset-modes span[data-mode="${value}"]`);
  if (active) {
    active.style.color = color;
    active.style.fontWeight = "bold";
  }
}

function renderAll() {
  renderMode("yaw-lime4",          state.lime4.yaw);
  renderMode("yaw-lime2",          state.lime2.yaw);
  renderMode("align-piece",        state.lime2.alignPiece);
  renderMode("yaw-lime2plus",      state.lime2plus.yaw);
  renderMode("shooter-lime2plus",  state.lime2plus.shooter);
}

// ==========================
// TEMPERATURA LIMELIGHT (UI)
// ==========================
const TEMP_WARN_C = 70;

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

/**
 * Limelight "hw" é um array numérico de status de hardware.
 * O layout pode variar por modelo/firmware, então tentamos:
 *  - preferir o último valor (muito comum ser temp/board)
 *  - fallback para índice 1 (muito comum ser cpuTemp)
 */
function extractTempFromHw(hwArr) {
  if (!Array.isArray(hwArr)) return null;

  // formato oficial: [temp, fps, ...]
  const temp = Number(hwArr[0]);
  if (Number.isFinite(temp)) return temp;

  return null;
}


// ==========================
// WEBSOCKET (nt3_ws.py -> browser)
// ==========================
const WS_URL = "ws://127.0.0.1:5810/nt/dashboard";

// MODOS (publicados pelo Java -> NT -> nt3_ws)
const TOPIC_AIMLOCK_LIME4     = "/Modes/AimLockLime4";
const TOPIC_AIMLOCK_LIME2     = "/Modes/AimLockLime2";
const TOPIC_ALIGN_LIME2       = "/Modes/AlignLime2";
const TOPIC_AIMLOCK_LIME2PLUS = "/Modes/AimLockLime2Plus";
const TOPIC_SHOOTER_LIME2PLUS = "/Modes/ShooterLime2Plus";
const TOPIC_ALIGN_PIECE       = "/Modes/AlignPiece";

// TEMP (publicados pela Limelight -> NT -> nt3_ws)
const TOPIC_HW_LIME4     = "/limelight-front/hw";
const TOPIC_HW_LIME2     = "/limelight-back/hw";
const TOPIC_HW_LIME2PLUS = "/limelight-lime2plus/hw";

function clampInt(v, min, max, fallback = min) {
  const n = Number(v);
  if (!Number.isFinite(n)) return fallback;
  const i = Math.trunc(n);
  if (i < min) return min;
  if (i > max) return max;
  return i;
}

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
        state.lime4.yaw = clampInt(value, 0, 1, 0);
        renderMode("yaw-lime4", state.lime4.yaw);
        return;
      }

      if (topic === TOPIC_AIMLOCK_LIME2) {
        state.lime2.yaw = clampInt(value, 0, 1, 0);
        renderMode("yaw-lime2", state.lime2.yaw);
        return;
      }

      // TOPIC_ALIGN_LIME2 retired — element removed from HTML

      // -------------------------
      // TEMPERATURA
      // -------------------------
      if (topic === TOPIC_HW_LIME4) {
        const t = extractTempFromHw(value);
        setTemp("temp-lime4", t);
        return;
      }

      if (topic === TOPIC_HW_LIME2) {
        const t = extractTempFromHw(value);
        setTemp("temp-lime2", t);
        return;
      }

      if (topic === TOPIC_AIMLOCK_LIME2PLUS) {
        state.lime2plus.yaw = clampInt(value, 0, 1, 0);
        renderMode("yaw-lime2plus", state.lime2plus.yaw);
        return;
      }

      if (topic === TOPIC_SHOOTER_LIME2PLUS) {
        state.lime2plus.shooter = clampInt(value, 0, 1, 0);
        renderMode("shooter-lime2plus", state.lime2plus.shooter);
        return;
      }

      if (topic === TOPIC_HW_LIME2PLUS) {
        const t = extractTempFromHw(value);
        setTemp("temp-lime2plus", t);
        return;
      }

      if (topic === TOPIC_ALIGN_PIECE) {
        state.lime2.alignPiece = clampInt(value, 0, 1, 0);
        renderMode("align-piece", state.lime2.alignPiece);
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
setTemp("temp-lime2", null);
setTemp("temp-lime2plus", null);
startWS();