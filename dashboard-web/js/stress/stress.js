"use strict";

import { onNTMessage } from "../ws.js";

const nt = {};

onNTMessage((topic, value) => {
  nt[topic] = value;
  if (topic.startsWith("/RobotStress/")) requestAnimationFrame(updateDashboard);
});

function updateDashboard() {
  setNum("/RobotStress/batteryVoltage",   "battery-voltage",    " V", 2);
  setNum("/RobotStress/totalCurrent",     "total-current",      " A", 1);
  setNum("/RobotStress/drivetrainCurrent","drivetrain-current", " A", 1);
  setNum("/RobotStress/stressScore",      "stress-score",       "",   0);

  const cs = nt["/RobotStress/chassisSpeed"];
  if (typeof cs === "number" && isFinite(cs))
    document.getElementById("chassis-speed").innerText = cs.toFixed(2) + " m/s";

  const ss = nt["/RobotStress/speedScale"];
  if (typeof ss === "number" && isFinite(ss))
    document.getElementById("speed-scale").innerText = Math.round(ss * 100) + "%";

  const level = nt["/RobotStress/stressLevel"];
  if (level !== undefined && level !== null) updateStressStatus(level);

  handleBatterySpeedWarning();
}

function setNum(topic, id, suffix, decimals) {
  const v = nt[topic];
  if (typeof v !== "number" || !isFinite(v)) return;
  document.getElementById(id).innerText = v.toFixed(decimals) + suffix;
}

function updateStressStatus(level) {
  const box = document.getElementById("stress-status");
  box.textContent = level;
  box.className = "";
  if (level === "LOW")      box.classList.add("status-ok");
  if (level === "MEDIUM")   box.classList.add("status-medium");
  if (level === "HIGH")     box.classList.add("status-high");
  if (level === "CRITICAL") box.classList.add("status-critical");
}

function handleBatterySpeedWarning() {
  const voltage    = nt["/RobotStress/batteryVoltage"];
  const speedScale = nt["/RobotStress/speedScale"];
  const warning    = document.getElementById("speed-warning");
  if (typeof voltage !== "number" || typeof speedScale !== "number") {
    warning.classList.add("hidden"); return;
  }
  if (voltage < 11.0 && speedScale < 1.0) warning.classList.remove("hidden");
  else warning.classList.add("hidden");
}