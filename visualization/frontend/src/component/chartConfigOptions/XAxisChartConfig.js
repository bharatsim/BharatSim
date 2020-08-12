import React from 'react';
import PropTypes from 'prop-types';

import Dropdown from '../../uiComponent/Dropdown';
import { convertStringArrayToOptions } from '../../utils/helper';

const XAxisChartConfig = ({ headers, updateConfigState, configKey }) => {
  const handleXChange = (value) => {
    updateConfigState({ [configKey]: value });
  };

  return (
    <Dropdown
      options={convertStringArrayToOptions(headers)}
      onChange={handleXChange}
      id="dropdown-x"
      label="select x axis"
    />
  );
};

XAxisChartConfig.propTypes = {
  headers: PropTypes.arrayOf(PropTypes.string).isRequired,
  updateConfigState: PropTypes.func.isRequired,
  configKey: PropTypes.string.isRequired,
};

export default XAxisChartConfig;
