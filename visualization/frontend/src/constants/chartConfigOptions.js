import React from 'react';

import XAxisChartConfig from '../component/chartConfigOptions/XAxisChartConfig';
import YAxisChartConfig from '../component/chartConfigOptions/YAxisChartConfig';

export const chartConfigOptions = {
  X_AXIS: 'xAxis',
  Y_AXIS: 'yAxis',
};

export function getChartConfigComponent(
  chartConfigType,
  { headers, updateConfigState, errors, values },
) {
  return {
    [chartConfigOptions.X_AXIS]: (
      <XAxisChartConfig
        headers={headers}
        updateConfigState={updateConfigState}
        configKey={chartConfigOptions.X_AXIS}
        error={errors[chartConfigOptions.X_AXIS]}
        value={values[chartConfigOptions.X_AXIS]}
      />
    ),
    [chartConfigOptions.Y_AXIS]: (
      <YAxisChartConfig
        headers={headers}
        updateConfigState={updateConfigState}
        configKey={chartConfigOptions.Y_AXIS}
        error={errors[chartConfigOptions.Y_AXIS]}
        value={values[chartConfigOptions.Y_AXIS]}
      />
    ),
  }[chartConfigType];
}
