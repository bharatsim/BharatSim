import React from 'react';
import PropTypes from 'prop-types';

import Dropdown from '../../uiComponent/Dropdown';
import { convertStringArrayToOptions } from '../../utils/helper';

const YAxisChartConfig = ({ headers, updateConfigState, configKey, error }) => {
  const handleYChange = (value) => {
    updateConfigState(configKey, value);
  };

  return (
    <Dropdown
      options={convertStringArrayToOptions(headers)}
      onChange={handleYChange}
      id="dropdown-y"
      label="select y axis"
      error={error}
    />
  );
};

YAxisChartConfig.defaultProps = {
  error: '',
};

YAxisChartConfig.propTypes = {
  headers: PropTypes.arrayOf(PropTypes.string).isRequired,
  updateConfigState: PropTypes.func.isRequired,
  configKey: PropTypes.string.isRequired,
  error: PropTypes.string,
};

export default YAxisChartConfig;
