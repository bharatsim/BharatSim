import React from 'react';
import { chartTypes } from '../constants/charts';
import LineChart from '../component/charts/lineChart/LineChart';
import BarChart from '../component/charts/barChart/BarChart';
import { chartConfigOptionTypes } from '../constants/chartConfigOptionTypes';
import { createConfigOptionValidationSchema } from './chartConfigOptions';

const chartConfigs = {
  [chartTypes.LINE_CHART]: {
    key: chartTypes.LINE_CHART,
    label: 'Line Chart',
    chart: (chartProps) => <LineChart {...chartProps} />,
    configOptions: [chartConfigOptionTypes.X_AXIS, chartConfigOptionTypes.Y_AXIS],
    configOptionValidationSchema: createConfigOptionValidationSchema([
      chartConfigOptionTypes.X_AXIS,
      chartConfigOptionTypes.Y_AXIS,
    ]),
  },
  [chartTypes.BAR_CHART]: {
    key: chartTypes.BAR_CHART,
    label: 'Bar Chart',
    chart: (chartProps) => <BarChart {...chartProps} />,
    configOptions: [chartConfigOptionTypes.X_AXIS, chartConfigOptionTypes.Y_AXIS],
    configOptionValidationSchema: createConfigOptionValidationSchema([
      chartConfigOptionTypes.X_AXIS,
      chartConfigOptionTypes.Y_AXIS,
    ]),
  },
};

export default chartConfigs;
