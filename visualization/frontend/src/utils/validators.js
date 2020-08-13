import { chartConfigOptions } from '../constants/chartConfigOptions';

const isAbsent = (value) => {
  return value === null || value === undefined || value === '';
};

const xAxisValidator = (value = '') => {
  if (isAbsent(value)) {
    return 'Please select value for x axis';
  }
  return '';
};

const yAxisValidator = (value = '') => {
  if (isAbsent(value)) {
    return 'Please select value for y axis';
  }
  return '';
};

const datasourceValidator = (value = '') => {
  if (isAbsent(value)) {
    return 'Please select data source';
  }
  return '';
};

const getChartConfigValidator = (ChartConfigOption) => {
  return {
    [chartConfigOptions.X_AXIS]: xAxisValidator,
    [chartConfigOptions.Y_AXIS]: yAxisValidator,
  }[ChartConfigOption];
};

export { getChartConfigValidator, datasourceValidator };
