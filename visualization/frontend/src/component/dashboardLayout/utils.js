import React from 'react';
import renderChart from '../charts/renderChart';

export function renderElement(element) {
  return (
    <div key={element.i} data-grid={element} data-testid={element.i}>
      <div style={{ height: '100%', width: '100%', boxSizing: 'border-box', padding: '10px' }}>
        {renderChart(element.chartType, { config: element.config })}
      </div>
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
