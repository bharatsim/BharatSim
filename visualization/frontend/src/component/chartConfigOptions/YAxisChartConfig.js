import React from 'react';
import PropTypes from 'prop-types';

import Dropdown from '../../uiComponent/Dropdown';
import { convertStringArrayToOptions } from '../../utils/helper';

const YAxisChartConfig = ({ headers, updateConfigState, configKey }) => {
  const handleYChange = (value) => {
    updateConfigState({ [configKey]: value });
  };

  return (
    <Dropdown
      options={convertStringArrayToOptions(headers)}
      onChange={handleYChange}
      id="dropdown-y"
      label="select y axis"
    />
  );
};

YAxisChartConfig.propTypes = {
  headers: PropTypes.arrayOf(PropTypes.string).isRequired,
  updateConfigState: PropTypes.func.isRequired,
  configKey: PropTypes.string.isRequired,
};

export default YAxisChartConfig;
