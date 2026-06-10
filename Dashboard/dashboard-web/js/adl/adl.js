"use strict";

import { loadDashboardConfig, splitTopic, topic } from "../config.js";
import { onConnectionChange, onNTMessage, ntSend } from "../ws.js";

const config = await loadDashboardConfig();
const stateMeta = config.adl?.states || {};
const maxLog = config.adl?.maxLogEntries || 60;

const T = {
  state: topic(config, "adlState"),
  decision: topic(config, "adlDecision"),
  vision: topic(config, "visionHasTarget"),
  aligned: topic(config, "visionAligned"),
  shooter: topic(config, "shooterReady"),
  piece: topic(config, "hasGamePiece"),
  endgame: topic(config, "endgame"),
  moving: topic(config, "driveMoving"),
  battery: topic(config, "robotBattery"),
  rpmCur: topic(config, "shooterCurrentRpm"),
  rpmTgt: topic(config, "shooterTargetRpm"),
  intent: topic(config, "adlIntent")
};

const el = {
  connDot: document.getElementById("conn-dot"),
  connLabel: document.getElementById("conn-label"),
  stateBadge: document.getElementById("state-badge"),
  stateIcon: document.getElementById("state-icon"),
  stateDesc: document.getElementById("state-desc"),
  endgameBanner: document.getElementById("endgame-banner"),
  dtypeBadge: document.getElementById("dtype-badge"),
  decisionReason: document.getElementById("decision-reason"),
  ctxVision: document.getElementById("ctx-vision"),
  ctxAligned: document.getElementById("ctx-aligned"),
  ctxShooter: document.getElementById("ctx-shooter"),
  ctxPiece: document.getElementById("ctx-piece"),
  ctxEndgame: document.getElementById("ctx-endgame"),
  ctxMoving: document.getElementById("ctx-moving"),
  ctxBattery: document.getElementById("ctx-battery"),
  ctxRpm: document.getElementById("ctx-rpm"),
  logList: document.getElementById("log-list")
};

let matchActive = false;
let lastState = "";
let _rpmCur = 0;
let _rpmTgt = 0;

onConnectionChange((online) => {
  if (!el.connDot || !el.connLabel) return;

  el.connDot.classList.toggle("live", online);
  el.connLabel.textContent = online ? "ONLINE" : "OFFLINE";
  addLog(online ? "WebSocket conectado" : "Conexao perdida - reconectando...", online ? "ok" : "danger");
});

onNTMessage((topicName, value) => {
  switch (topicName) {
    case T.state:
      setState(String(value));
      break;
    case T.decision:
      setDecision(String(value));
      break;
    case T.vision:
      setPill(el.ctxVision, Boolean(value));
      break;
    case T.aligned:
      setPill(el.ctxAligned, Boolean(value));
      break;
    case T.shooter:
      setPill(el.ctxShooter, Boolean(value));
      break;
    case T.piece:
      setPill(el.ctxPiece, Boolean(value));
      break;
    case T.moving:
      setPill(el.ctxMoving, Boolean(value));
      break;
    case T.endgame:
      setEndgame(Boolean(value));
      break;
    case T.battery:
      setBattery(Number(value));
      break;
    case T.rpmCur:
      _rpmCur = Number(value);
      updateRpm();
      break;
    case T.rpmTgt:
      _rpmTgt = Number(value);
      updateRpm();
      break;
  }
});

function setEndgame(active) {
  setPill(el.ctxEndgame, active, true);
  if (el.endgameBanner) el.endgameBanner.classList.toggle("hidden", !active);
  setMatchActive(active);
}

function setBattery(voltage) {
  if (!el.ctxBattery || !Number.isFinite(voltage)) return;
  el.ctxBattery.textContent = voltage.toFixed(2) + " V";
  el.ctxBattery.style.color =
    voltage < 10 ? "var(--danger)" : voltage < 11 ? "var(--warn)" : "var(--accent)";
}

function updateRpm() {
  if (!el.ctxRpm) return;
  el.ctxRpm.textContent = _rpmCur.toFixed(0) + " / " + _rpmTgt.toFixed(0) + " rpm";
  el.ctxRpm.style.color =
    Math.abs(_rpmCur - _rpmTgt) < 100 ? "var(--ok)" : "var(--accent)";
}

function setMatchActive(active) {
  matchActive = active;
  document.querySelectorAll(".ibtn").forEach((btn) => {
    btn.disabled = active;
    btn.style.opacity = active ? "0.3" : "1";
    btn.style.cursor = active ? "not-allowed" : "pointer";
  });
}

function addLog(msg, type = "") {
  if (!el.logList) return;

  const ts = new Date().toTimeString().slice(0, 8);
  const div = document.createElement("div");
  div.className = "log-entry";
  div.innerHTML = `<span class="log-ts">${ts}</span><span class="log-msg ${type}">${msg}</span>`;
  el.logList.prepend(div);

  while (el.logList.children.length > maxLog) el.logList.lastChild.remove();
}

function clearLog() {
  if (el.logList) el.logList.innerHTML = "";
}
window.clearLog = clearLog;

function setPill(elem, on, warnMode = false) {
  if (!elem) return;
  elem.textContent = on ? "ON" : "OFF";
  elem.className = "ctx-pill" + (on ? (warnMode ? " warn" : " on") : "");
}

function setState(state) {
  if (state === lastState) return;
  lastState = state;

  const meta = stateMeta[state] || { icon: "?", description: state };
  el.stateBadge.textContent = state;
  el.stateBadge.className = "state-badge " + state;
  el.stateIcon.textContent = meta.icon;
  el.stateIcon.className = "state-icon " + state;
  el.stateDesc.textContent = meta.description;

  const logType =
    state === "EMERGENCY" ? "danger" :
    state === "BLOCKED" ? "warn" :
    state === "IDLE" ? "" : "ok";
  addLog("Estado -> " + state, logType);
}

function setDecision(raw) {
  let dtype = "EXECUTE";
  let reason = raw;

  if (raw.startsWith("HOLD: ")) {
    dtype = "HOLD";
    reason = raw.slice(6);
  }
  if (raw.startsWith("REJECT: ")) {
    dtype = "REJECT";
    reason = raw.slice(8);
  }

  el.dtypeBadge.textContent = dtype;
  el.dtypeBadge.className = "dtype-badge " + dtype;
  el.decisionReason.textContent = reason;

  const logType = dtype === "REJECT" ? "danger" : dtype === "HOLD" ? "warn" : "";
  addLog(dtype + ": " + reason, logType);
}

function sendIntent(cmd) {
  if (matchActive) {
    addLog("Partida ativa - use o controle fisico", "warn");
    return;
  }

  const target = splitTopic(T.intent);
  ntSend({ action: "put", table: target.table, key: target.key, value: cmd });
  addLog("-> " + cmd, "info");
}
window.sendIntent = sendIntent;

addLog("Dashboard iniciado", "info");
