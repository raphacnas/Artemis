"use strict";

import { loadDashboardConfig } from "./config.js";

const _handlers = [];
const _connHandlers = [];

let _ws = null;
let _wsUrl = null;
let _reconnectMs = 1200;

export function onNTMessage(fn) {
  _handlers.push(fn);
}

export function ntSend(payload) {
  if (_ws && _ws.readyState === WebSocket.OPEN) {
    _ws.send(JSON.stringify(payload));
  }
}

export function onConnectionChange(fn) {
  _connHandlers.push(fn);
}

function setConnectionState(online) {
  _connHandlers.forEach((fn) => fn(online));
}

function connect() {
  if (!_wsUrl) return;

  _ws = new WebSocket(_wsUrl);

  _ws.onopen = () => setConnectionState(true);

  _ws.onmessage = (ev) => {
    let msg;
    try {
      msg = JSON.parse(ev.data);
    } catch {
      return;
    }

    if (!msg || msg.topic === undefined || msg.value === undefined) return;
    _handlers.forEach((fn) => fn(msg.topic, msg.value));
  };

  _ws.onclose = () => {
    setConnectionState(false);
    setTimeout(connect, _reconnectMs);
  };

  _ws.onerror = () => {
    try {
      _ws.close();
    } catch {}
  };
}

loadDashboardConfig()
  .then((config) => {
    _wsUrl = config.websocket?.url || "ws://127.0.0.1:5901/nt/dashboard";
    _reconnectMs = config.websocket?.reconnectMs || 1200;
    connect();
  })
  .catch((err) => {
    console.error(err);
    setConnectionState(false);
  });
