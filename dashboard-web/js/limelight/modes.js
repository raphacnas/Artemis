"use strict";

import { onNTMessage } from "../ws.js";

const state = {
  lime4:    { alinhador: 0, yaw: 0, teste1: 0 },
  lime2:    { alinhador: 0, yaw: 0 },
  lime2plus:{ yaw: 0, shooter: 0 }
};

const TOPIC_AIMLOCK_LIME4     = "/Modes/AimLockLime4";
const TOPIC_AIMLOCK_LIME2     = "/Modes/AimLockLime2";
const TOPIC_ALIGN_LIME2       = "/Modes/AlignLime2";
const TOPIC_AIMLOCK_LIME2PLUS = "/Modes/AimLockLime2Plus";
const TOPIC_SHOOTER_LIME2PLUS = "/Modes/ShooterLime2Plus";
const TOPIC_HW_LIME4          = "/limelight-front/hw";
const TOPIC_HW_LIME2          = "/limelight-back/hw";
const TOPIC_HW_LIME2PLUS      = "/limelight-lime2plus/hw";

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

function setTemp(id, hwArr) {
  const el = document.getElementById(id);
  if (!el) return;
  const t = Array.isArray(hwArr) ? Number(hwArr[0]) : NaN;
  if (!Number.isFinite(t)) { el.textContent = "Temp: --°C"; el.classList.remove("warn"); return; }
  el.textContent = `Temp: ${t.toFixed(0)}°C${t >= TEMP_WARN_C ? " ⚠️" : ""}`;
  el.classList.toggle("warn", t >= TEMP_WARN_C);
}

onNTMessage((topic, value) => {
  switch (topic) {
    case TOPIC_AIMLOCK_LIME4:
      state.lime4.yaw = clampInt(value, 0, 1);
      renderMode("yaw-lime4", state.lime4.yaw); break;

    case TOPIC_AIMLOCK_LIME2:
      state.lime2.yaw = clampInt(value, 0, 1);
      renderMode("yaw-lime2", state.lime2.yaw); break;

    case TOPIC_ALIGN_LIME2:
      state.lime2.alinhador = clampInt(value, 0, 2);
      renderMode("alinhador-lime2", state.lime2.alinhador); break;

    case TOPIC_AIMLOCK_LIME2PLUS:
      state.lime2plus.yaw = clampInt(value, 0, 1);
      renderMode("yaw-lime2plus", state.lime2plus.yaw); break;

    case TOPIC_SHOOTER_LIME2PLUS:
      state.lime2plus.shooter = clampInt(value, 0, 1);
      renderMode("shooter-lime2plus", state.lime2plus.shooter); break;

    case TOPIC_HW_LIME4:     setTemp("temp-lime4",     value); break;
    case TOPIC_HW_LIME2:     setTemp("temp-lime2",     value); break;
    case TOPIC_HW_LIME2PLUS: setTemp("temp-lime2plus", value); break;
  }
});

// render inicial
["yaw-lime4","teste1-lime4","alinhador-lime2","yaw-lime2",
 "yaw-lime2plus","shooter-lime2plus"].forEach(id => renderMode(id, 0));
setTemp("temp-lime4", null);
setTemp("temp-lime2plus", null);