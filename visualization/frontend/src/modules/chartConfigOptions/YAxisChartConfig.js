import React from 'react';
import PropTypes from 'prop-types';

import Dropdown from '../../uiComponent/Dropdown';
import { convertObjectArrayToOptionStructure } from '../../utils/helper';

function YAxisChartConfig({ headers, updateConfigState, configKey, error, value }) {
  function handleYChange(selectedValue) {
    updateConfigState(configKey, selectedValue);
  }

  return (
    <Dropdown
      options={convertObjectArrayToOptionStructure(headers, 'name')}
      onChange={handleYChange}
      id="dropdown-y"
      label="select y axis"
      error={error}
      value={value || []}
      multiple
    />
  );
}

YAxisChartConfig.defaultProps = {
  error: '',
  value: null,
};

YAxisChartConfig.propTypes = {
  headers: PropTypes.arrayOf(
    PropTypes.shape({
      name: PropTypes.string.isRequired,
      type: PropTypes.string.isRequired,
    }),
  ).isRequired,
  updateConfigState: PropTypes.func.isRequired,
  configKey: PropTypes.string.isRequired,
  error: PropTypes.string,
  value: PropTypes.arrayOf(
    PropTypes.shape({
      name: PropTypes.string.isRequired,
      type: PropTypes.string.isRequired,
    }),
  ),
};

export default YAxisChartConfig;
