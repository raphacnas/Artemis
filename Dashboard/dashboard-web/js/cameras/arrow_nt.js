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

  const bboxDiv = document.createElement("div");
  bboxDiv.className = "bbox";
  parent.appendChild(bboxDiv);

  let hasTarget = false, tx = null, bbox = null, alignEnabled = false;

  function render() {
    // bbox: aparece sempre que has_target=true
    if (hasTarget) {
      updateBBox();
    } else {
      bboxDiv.classList.remove("visible");
    }

    // seta: só aparece se AlignLime2 ativo
    if (!hasTarget || !alignEnabled) {
      arrow.classList.remove("visible");
      arrow.classList.add("hidden");
      return;
    }
    updateArrow();
  }

  function updateArrow() {
    if (tx === null || Math.abs(tx) <= TX_DEADBAND) {
      arrow.classList.remove("visible");
      arrow.classList.add("hidden");
      return;
    }
    arrow.classList.remove("hidden", "arrow-left", "arrow-right");
    const level = Math.abs(tx) >= TX_STRONG ? 3 : Math.abs(tx) >= TX_MEDIUM ? 2 : 1;
    if (tx > 0) {
      arrow.textContent = ">".repeat(level);
      arrow.classList.add("arrow-left");
    } else {
      arrow.textContent = "<".repeat(level);
      arrow.classList.add("arrow-right");
    }
    arrow.classList.add("visible");
  }

  function updateBBox() {
    if (!img.complete || !bbox || bbox.length !== 4 || !img.naturalWidth) {
      bboxDiv.classList.remove("visible");
      return;
    }
    const [x1, y1, x2, y2] = bbox;
    const rect = img.getBoundingClientRect();
    const sx = rect.width  / img.naturalWidth;
    const sy = rect.height / img.naturalHeight;

    // A imagem tem transform: rotate(180deg), então espelha X e Y
    const flippedX1 = img.naturalWidth  - x2;
    const flippedY1 = img.naturalHeight - y2;

    bboxDiv.style.left   = (flippedX1 * sx) + "px";
    bboxDiv.style.top    = (flippedY1 * sy) + "px";
    bboxDiv.style.width  = ((x2 - x1) * sx) + "px";
    bboxDiv.style.height = ((y2 - y1) * sy) + "px";
    bboxDiv.classList.add("visible");
  }

  onNTMessage((topic, value) => {
    if ([KEY_HAS_TARGET, KEY_TX, KEY_BBOX, KEY_ALIGN_LIME2].includes(topic)) {
      console.log(`[NT] ${topic} =`, value);
    }

    switch (topic) {
      case KEY_ALIGN_LIME2: alignEnabled = Number(value) !== 0; render(); break;
      case KEY_HAS_TARGET:  hasTarget = Boolean(value);          render(); break;
      case KEY_TX:          tx = value == null ? null : Number(value); render(); break;
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