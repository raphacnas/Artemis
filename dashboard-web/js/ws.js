"use strict";

// ═══════════════════════════════════════
// WS.JS — WebSocket único compartilhado
// HYDRA #9163
// ═══════════════════════════════════════

const WS_URL = "ws://127.0.0.1:5901/nt/dashboard";

const _handlers = [];

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

const _connHandlers = [];

let _ws = null;

function connect() {
  _ws = new WebSocket(WS_URL);

  _ws.onopen = () => {
    _connHandlers.forEach(fn => fn(true));
  };

  _ws.onmessage = (ev) => {
    let msg;
    try { msg = JSON.parse(ev.data); } catch { return; }
    if (!msg || msg.topic === undefined || msg.value === undefined) return;
    _handlers.forEach(fn => fn(msg.topic, msg.value));
  };

  _ws.onclose = () => {
    _connHandlers.forEach(fn => fn(false));
    setTimeout(connect, 1200);
  };

  _ws.onerror = () => { try { _ws.close(); } catch {} };
}

connect();