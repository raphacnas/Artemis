"use strict";

import { onNTMessage, ntSend, onConnectionChange } from "../ws.js";

const T = {
  STATE:    "/ADL/state",
  DECISION: "/ADL/decision",
  VISION:   "/Vision/HasTarget",
  ALIGNED:  "/Vision/Aligned",
  SHOOTER:  "/Mechanisms/ShooterReady",
  PIECE:    "/Mechanisms/HasGamePiece",
  ENDGAME:  "/Game/Endgame",
  MOVING:   "/Drive/Moving",
  BATTERY:  "/Robot/BatteryVoltage",
  RPM_CUR:  "/Shooter/CurrentRPM",
  RPM_TGT:  "/Shooter/TargetRPM",
};

const STATE_META = {
  IDLE:      { icon: "○", desc: "Aguardando intenção" },
  MOVING:    { icon: "⟶", desc: "Robô em movimento" },
  ACQUIRING: { icon: "⬇", desc: "Coletando game piece" },
  SCORING:   { icon: "◎", desc: "Executando pontuação" },
  CLIMBING:  { icon: "↑", desc: "Iniciando escalada" },
  BLOCKED:   { icon: "✕", desc: "Estado bloqueado" },
  EMERGENCY: { icon: "⚠", desc: "ABORTAR — EMERGÊNCIA" },
};

const el = {
  connDot:        document.getElementById("conn-dot"),
  connLabel:      document.getElementById("conn-label"),
  stateBadge:     document.getElementById("state-badge"),
  stateIcon:      document.getElementById("state-icon"),
  stateDesc:      document.getElementById("state-desc"),
  endgameBanner:  document.getElementById("endgame-banner"),
  dtypeBadge:     document.getElementById("dtype-badge"),
  decisionReason: document.getElementById("decision-reason"),
  ctxVision:      document.getElementById("ctx-vision"),
  ctxAligned:     document.getElementById("ctx-aligned"),
  ctxShooter:     document.getElementById("ctx-shooter"),
  ctxPiece:       document.getElementById("ctx-piece"),
  ctxEndgame:     document.getElementById("ctx-endgame"),
  ctxMoving:      document.getElementById("ctx-moving"),
  ctxBattery:     document.getElementById("ctx-battery"),
  ctxRpm:         document.getElementById("ctx-rpm"),
  logList:        document.getElementById("log-list"),
};

let matchActive = false;
let _rpmCur = 0, _rpmTgt = 0;
const MAX_LOG = 60;

onConnectionChange((online) => {
  if (!el.connDot || !el.connLabel) return;
  if (online) {
    el.connDot.classList.add("live");
    el.connLabel.textContent = "ONLINE";
    addLog("WebSocket conectado", "ok");
  } else {
    el.connDot.classList.remove("live");
    el.connLabel.textContent = "OFFLINE";
    addLog("Conexão perdida — reconectando...", "danger");
  }
});

onNTMessage((topic, value) => {
  switch (topic) {
    case T.STATE:    setState(String(value));    break;
    case T.DECISION: setDecision(String(value)); break;
    case T.VISION:   setPill(el.ctxVision,  Boolean(value)); break;
    case T.ALIGNED:  setPill(el.ctxAligned, Boolean(value)); break;
    case T.SHOOTER:  setPill(el.ctxShooter, Boolean(value)); break;
    case T.PIECE:    setPill(el.ctxPiece,   Boolean(value)); break;
    case T.MOVING:   setPill(el.ctxMoving,  Boolean(value)); break;

    case T.ENDGAME: {
      const eg = Boolean(value);
      setPill(el.ctxEndgame, eg, true);
      if (el.endgameBanner) el.endgameBanner.classList.toggle("hidden", !eg);
      setMatchActive(eg);
      break;
    }

    case T.BATTERY: {
      const v = Number(value);
      if (el.ctxBattery) {
        el.ctxBattery.textContent = v.toFixed(2) + " V";
        el.ctxBattery.style.color =
          v < 10 ? "var(--danger)" : v < 11 ? "var(--warn)" : "var(--accent)";
      }
      break;
    }

    case T.RPM_CUR:
      _rpmCur = Number(value);
      updateRpm(); break;

    case T.RPM_TGT:
      _rpmTgt = Number(value);
      updateRpm(); break;
  }
});

function updateRpm() {
  if (!el.ctxRpm) return;
  el.ctxRpm.textContent = _rpmCur.toFixed(0) + " / " + _rpmTgt.toFixed(0) + " rpm";
  el.ctxRpm.style.color =
    Math.abs(_rpmCur - _rpmTgt) < 100 ? "var(--ok)" : "var(--accent)";
}

function setMatchActive(active) {
  matchActive = active;
  document.querySelectorAll(".ibtn").forEach(btn => {
    btn.disabled      = active;
    btn.style.opacity = active ? "0.3" : "1";
    btn.style.cursor  = active ? "not-allowed" : "pointer";
  });
}

function addLog(msg, type = "") {
  if (!el.logList) return;
  const ts  = new Date().toTimeString().slice(0, 8);
  const div = document.createElement("div");
  div.className = "log-entry";
  div.innerHTML = `<span class="log-ts">${ts}</span><span class="log-msg ${type}">${msg}</span>`;
  el.logList.prepend(div);
  while (el.logList.children.length > MAX_LOG) el.logList.lastChild.remove();
}

function clearLog() { if (el.logList) el.logList.innerHTML = ""; }
window.clearLog = clearLog;

function setPill(elem, on, warnMode = false) {
  if (!elem) return;
  elem.textContent = on ? "ON" : "OFF";
  elem.className   = "ctx-pill" + (on ? (warnMode ? " warn" : " on") : "");
}

let lastState = "";
function setState(state) {
  if (state === lastState) return;
  lastState = state;
  const meta = STATE_META[state] || { icon: "?", desc: state };
  el.stateBadge.textContent = state;
  el.stateBadge.className   = "state-badge " + state;
  el.stateIcon.textContent  = meta.icon;
  el.stateIcon.className    = "state-icon " + state;
  el.stateDesc.textContent  = meta.desc;
  const logType =
    state === "EMERGENCY" ? "danger" :
    state === "BLOCKED"   ? "warn"   :
    state === "IDLE"      ? ""       : "ok";
  addLog("Estado → " + state, logType);
}

function setDecision(raw) {
  let dtype = "EXECUTE", reason = raw;
  if (raw.startsWith("HOLD: "))   { dtype = "HOLD";   reason = raw.slice(6); }
  if (raw.startsWith("REJECT: ")) { dtype = "REJECT"; reason = raw.slice(8); }
  el.dtypeBadge.textContent     = dtype;
  el.dtypeBadge.className       = "dtype-badge " + dtype;
  el.decisionReason.textContent = reason;
  const logType = dtype === "REJECT" ? "danger" : dtype === "HOLD" ? "warn" : "";
  addLog(dtype + ": " + reason, logType);
}

function sendIntent(cmd) {
  if (matchActive) { addLog("⚠ Partida ativa — use o controle físico", "warn"); return; }
  ntSend({ action: "put", table: "ADL", key: "intent", value: cmd });
  addLog("→ " + cmd, "info");
}
window.sendIntent = sendIntent;

addLog("Dashboard iniciado", "info");