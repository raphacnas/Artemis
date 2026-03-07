"use strict";

import { onNTMessage } from "../ws.js";

const TX_DEADBAND = 2;
const TX_MEDIUM   = 5;
const TX_STRONG   = 8;

const KEY_HAS_TARGET  = "/limelight-back/has_target";
const KEY_TX          = "/limelight-back/piece_tx";
const KEY_BBOX        = "/limelight-back/bbox";
const KEY_ALIGN_LIME2 = "/Modes/AlignLime2";

function setupLimelightArrow(config) {
  const { arrowId, parentSelector, imgSelector } = config;

  const arrow  = document.getElementById(arrowId);
  const parent = document.querySelector(parentSelector);
  const img    = parent ? parent.querySelector(imgSelector) : null;
  if (!arrow || !parent || !img) return;

  arrow.style.transition = "opacity 150ms ease";
  arrow.style.opacity = "0";

  const bboxDiv = document.createElement("div");
  bboxDiv.className = "bbox";
  bboxDiv.style.cssText = "position:absolute;pointer-events:none;opacity:0;transition:opacity 150ms ease;";
  parent.appendChild(bboxDiv);

  let hasTarget = false, tx = null, bbox = null, alignEnabled = false;

  function render() {
    if (!hasTarget || !alignEnabled) {
      arrow.classList.add("hidden");
      arrow.style.opacity = "0";
      bboxDiv.style.opacity = "0";
      return;
    }
    updateArrow();
    updateBBox();
  }

  function updateArrow() {
    if (tx === null || Math.abs(tx) <= TX_DEADBAND) {
      arrow.classList.add("hidden");
      arrow.style.opacity = "0";
      return;
    }
    arrow.classList.remove("hidden", "arrow-left", "arrow-right");
    const level = Math.abs(tx) >= TX_STRONG ? 3 : Math.abs(tx) >= TX_MEDIUM ? 2 : 1;
    if (tx > 0) { arrow.textContent = ">".repeat(level); arrow.classList.add("arrow-left"); }
    else        { arrow.textContent = "<".repeat(level); arrow.classList.add("arrow-right"); }
    arrow.style.opacity = "1";
  }

  function updateBBox() {
    if (!img.complete || !bbox || bbox.length !== 4 || !img.naturalWidth) {
      bboxDiv.style.opacity = "0"; return;
    }
    const [x1, y1, x2, y2] = bbox;
    const rect = img.getBoundingClientRect();
    const sx = rect.width / img.naturalWidth;
    const sy = rect.height / img.naturalHeight;
    bboxDiv.style.left   = (x1 * sx) + "px";
    bboxDiv.style.top    = (y1 * sy) + "px";
    bboxDiv.style.width  = ((x2 - x1) * sx) + "px";
    bboxDiv.style.height = ((y2 - y1) * sy) + "px";
    bboxDiv.style.opacity = "1";
  }

  onNTMessage((topic, value) => {
    switch (topic) {
      case KEY_ALIGN_LIME2:  alignEnabled = Number(value) !== 0; render(); break;
      case KEY_HAS_TARGET:   hasTarget = Boolean(value);          render(); break;
      case KEY_TX:           tx = value == null ? null : Number(value); render(); break;
      case KEY_BBOX:
        bbox = Array.isArray(value) && value.length === 4 ? value.map(Number) : null;
        render(); break;
    }
  });

  render();
}

setupLimelightArrow({
  arrowId: "arrow-lime2",
  parentSelector: "#lime2 .arrow-parent",
  imgSelector: "img",
});