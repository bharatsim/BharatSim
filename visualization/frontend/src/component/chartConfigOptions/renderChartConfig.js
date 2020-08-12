import React from 'react';
import { getChartConfigComponent } from '../../constants/chartConfigOptions';

export default function renderChartConfig(chartConfigs, chartConfigProps) {
  return (
    <div>
      {chartConfigs.map((chartConfigType) => (
        <div key={chartConfigType}>
          {getChartConfigComponent(chartConfigType, chartConfigProps)}
        </div>
      ))}
    </div>
  );
}
