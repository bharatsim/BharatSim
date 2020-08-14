import { chartConfigOptions } from '../constants/chartConfigOptions';

const isAbsent = (value) => {
  return value === null || value === undefined || value === '';
};

function isNotAxisTypeNumber(value) {
  return value.type !== 'number';
}

const xAxisValidator = (value = '') => {
  if (isAbsent(value)) {
    return 'Please select value for x axis';
  }
  return '';
};

const yAxisValidator = (value = null) => {
  if (isAbsent(value)) {
    return 'Please select value for y axis';
  }
  if (isNotAxisTypeNumber(value)) {
    return 'Please select number type option';
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
