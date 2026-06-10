"use strict";

import { loadDashboardConfig, splitTopic, topic } from "../config.js";
import { onNTMessage } from "../ws.js";

const config = await loadDashboardConfig();
const stressConfig = config.stress || {};
const nt = {};

const topics = {
  batteryVoltage: topic(config, "stressBatteryVoltage"),
  stressLevel: topic(config, "stressLevel"),
  speedScale: topic(config, "stressSpeedScale"),
  chassisSpeed: topic(config, "stressChassisSpeed")
};
const stressPrefix = `/${splitTopic(topics.batteryVoltage).table}/`;

onNTMessage((topicName, value) => {
  nt[topicName] = value;
  if (topicName.startsWith(stressPrefix)) requestAnimationFrame(updateDashboard);
});

function updateDashboard() {
  for (const metric of stressConfig.metrics || []) {
    setNum(topic(config, metric.topic), metric.elementId, metric.suffix, metric.decimals);
  }

  const cs = nt[topics.chassisSpeed];
  if (typeof cs === "number" && isFinite(cs)) {
    document.getElementById("chassis-speed").innerText = cs.toFixed(2) + " m/s";
  }

  const ss = nt[topics.speedScale];
  if (typeof ss === "number" && isFinite(ss)) {
    document.getElementById("speed-scale").innerText = Math.round(ss * 100) + "%";
  }

  const level = nt[topics.stressLevel];
  if (level !== undefined && level !== null) updateStressStatus(level);

  handleBatterySpeedWarning();
}

function setNum(topicName, id, suffix, decimals) {
  const v = nt[topicName];
  if (typeof v !== "number" || !isFinite(v)) return;
  document.getElementById(id).innerText = v.toFixed(decimals) + suffix;
}

function updateStressStatus(level) {
  const box = document.getElementById("stress-status");
  box.textContent = level;
  box.className = "";

  const className = stressConfig.levels?.[level];
  if (className) box.classList.add(className);
}

function handleBatterySpeedWarning() {
  const voltage = nt[topics.batteryVoltage];
  const speedScale = nt[topics.speedScale];
  const warning = document.getElementById("speed-warning");

  if (typeof voltage !== "number" || typeof speedScale !== "number") {
    warning.classList.add("hidden");
    return;
  }

  const threshold = stressConfig.batteryWarningVoltage || 11.0;
  warning.classList.toggle("hidden", !(voltage < threshold && speedScale < 1.0));
}
