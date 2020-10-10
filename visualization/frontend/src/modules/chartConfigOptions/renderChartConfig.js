import React from 'react';
import chartConfigOptions from '../../config/chartConfigOptions';

export default function renderChartConfig(chartConfigs, chartConfigProps) {
  return (
    <div>
      {chartConfigs.map((chartConfigType) => (
        <div key={chartConfigType}>
          {chartConfigOptions[chartConfigType].component(chartConfigProps)}
        </div>
      ))}
    </div>
  );
}
