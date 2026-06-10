"use strict";

const CONFIG_URL = new URL("../config/dashboard.json", import.meta.url);

let configPromise = null;

export function loadDashboardConfig() {
  if (!configPromise) {
    configPromise = fetch(CONFIG_URL).then((res) => {
      if (!res.ok) {
        throw new Error(`Dashboard config failed to load: ${res.status}`);
      }
      return res.json();
    });
  }
  return configPromise;
}

export function topic(config, name) {
  const value = config.topics?.[name];
  if (!value) {
    throw new Error(`Missing dashboard topic alias: ${name}`);
  }
  return value;
}

export function splitTopic(topicName) {
  const parts = String(topicName).replace(/^\/+/, "").split("/");
  return {
    table: parts.slice(0, -1).join("/"),
    key: parts.at(-1)
  };
}
