import React from 'react';
import PropTypes from 'prop-types';

import Dropdown from '../../uiComponent/Dropdown';
import { convertObjectArrayToOptionStructure } from '../../utils/helper';

const XAxisChartConfig = ({ headers, updateConfigState, configKey, error, value }) => {
  const handleXChange = (selectedValue) => {
    updateConfigState(configKey, selectedValue);
  };

  return (
    <Dropdown
      options={convertObjectArrayToOptionStructure(headers, 'name', 'name')}
      onChange={handleXChange}
      id="dropdown-x"
      label="select x axis"
      error={error}
      value={value}
    />
  );
};

XAxisChartConfig.defaultProps = {
  error: '',
  value: '',
};

XAxisChartConfig.propTypes = {
  headers: PropTypes.arrayOf(
    PropTypes.shape({
      name: PropTypes.string.isRequired,
      type: PropTypes.string.isRequired,
    }),
  ).isRequired,
  updateConfigState: PropTypes.func.isRequired,
  configKey: PropTypes.string.isRequired,
  error: PropTypes.string,
  value: PropTypes.string,
};

export default XAxisChartConfig;
