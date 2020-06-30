import React from "react";
import LineChart from "../lineChart/LineChart.js"
export function createElement(element) {
  return (
    <div key={element.i} data-grid={element} data-testid={element.i}>
      <LineChart config={element.config} />
    </div>
  );
}

export function getInitialLayout() {
  return [0].map(function (i, key, list) {
    return {
      i: i.toString(),
      x: i * 2,
      y: 0,
      w: 2,
      h: 2,
    };
  });
}