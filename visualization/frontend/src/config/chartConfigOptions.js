/* eslint-disable react/prop-types */
import React from 'react';
import XAxisChartConfig from '../component/chartConfigOptions/XAxisChartConfig';
import YAxisChartConfig from '../component/chartConfigOptions/YAxisChartConfig';
import { xAxisValidator, yAxisValidator } from '../utils/validators';
import { chartConfigOptionTypes } from '../constants/chartConfigOptionTypes';

const chartConfigOptions = {
  [chartConfigOptionTypes.X_AXIS]: {
    component: ({ headers, updateConfigState, errors, values }) => (
      <XAxisChartConfig
        headers={headers}
        updateConfigState={updateConfigState}
        configKey={chartConfigOptionTypes.X_AXIS}
        error={errors[chartConfigOptionTypes.X_AXIS]}
        value={values[chartConfigOptionTypes.X_AXIS]}
      />
    ),
    validator: xAxisValidator,
  },
  [chartConfigOptionTypes.Y_AXIS]: {
    component: ({ headers, updateConfigState, errors, values }) => (
      <YAxisChartConfig
        headers={headers}
        updateConfigState={updateConfigState}
        configKey={chartConfigOptionTypes.Y_AXIS}
        error={errors[chartConfigOptionTypes.Y_AXIS]}
        value={values[chartConfigOptionTypes.Y_AXIS]}
      />
    ),
    validator: yAxisValidator,
  },
};

function createConfigOptionValidationSchema(configOptions) {
  const configOptionValidationSchema = {};
  configOptions.forEach((configOption) => {
    configOptionValidationSchema[configOption] = chartConfigOptions[configOption].validator;
  });

  return configOptionValidationSchema;
}

export default chartConfigOptions;
export { createConfigOptionValidationSchema };
