/* eslint-disable react/prop-types */

import React from 'react';
import renderChart from '../component/charts/renderChart';

export function renderElement({ layout, chartType, config }) {
  return (
    <div key={layout.i} data-grid={layout} data-testid={layout.i}>
      <div style={{ height: '100%', width: '100%', boxSizing: 'border-box', padding: '10px' }}>
        {renderChart(chartType, { config })}
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
