import React from 'react';
import { chartTypes } from '../constants/charts';
import LineChart from '../component/charts/lineChart/LineChart';
import BarChart from '../component/charts/barChart/BarChart';
import { chartConfigOptions } from '../constants/chartConfigOptions';

const chartConfigs = {
  [chartTypes.LINE_CHART]: {
    key: chartTypes.LINE_CHART,
    chart: (chartProps) => <LineChart {...chartProps} />,
    configOptions: [chartConfigOptions.X_AXIS, chartConfigOptions.Y_AXIS],
  },
  [chartTypes.BAR_CHART]: {
    key: chartTypes.BAR_CHART,
    chart: (chartProps) => <BarChart {...chartProps} />,
    configOptions: [chartConfigOptions.X_AXIS, chartConfigOptions.Y_AXIS],
  },
};

export default chartConfigs;
