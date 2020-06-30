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
    return [createLayout({
      id: `widget-0`,
      xPosition: 0,
      yPosition: 0,
      width: 2,
      height: 2,
    })];
}

export function getNewWidgetLayout(numberOfWidgetAdded, cols, count) {
  return createLayout({
    id: `widget-${count}`,
    xPosition: (numberOfWidgetAdded * 2) % (cols),
    yPosition: Infinity, // puts it at the bottom
    width: 2,
    height: 2,
  })
}

function createLayout({id, xPosition, yPosition, width, height}){
  return {
    i: id,
    x: xPosition,
    y: yPosition,
    w: width,
    h: height,
  }
}