import React from 'react';
import LineChart from '../lineChart/LineChart';
import BasicBarChart from '../barChart/BarChart';

const selectChart = (chartType, config) => {
  return {
    line: <LineChart config={config} />,
    bar: <BasicBarChart config={config} />,
  }[chartType];
};

export function createElement(element) {
  return (
    <div key={element.i} data-grid={element} data-testid={element.i}>
      {selectChart(element.chartType, element.config)}
    </div>
  );
}

export function getNewWidgetLayout(numberOfWidgetAdded, cols, count) {
  return createLayout({
    id: `widget-${count}`,
    xPosition: (numberOfWidgetAdded * 2) % cols,
    yPosition: Infinity, // puts it at the bottom
    width: 2,
    height: 2,
  });
}

function createLayout({ id, xPosition, yPosition, width, height }) {
  return {
    i: id,
    x: xPosition,
    y: yPosition,
    w: width,
    h: height,
  };
}
