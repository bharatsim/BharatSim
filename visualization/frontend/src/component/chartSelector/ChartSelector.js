import React from 'react';
import PropTypes from 'prop-types';

import { Button } from '@material-ui/core';

import ButtonGroup from '../../uiComponent/ButtonGroup';
import chartConfigs from '../../config/chartConfigs';

function ChartSelector({ onClick }) {
  return (
    <ButtonGroup>
      {Object.values(chartConfigs).map((chart) => {
        return (
          <Button
            key={chart.key}
            onClick={() => onClick(chart.key)}
            variant="contained"
            color="primary"
          >
            {chart.label}
          </Button>
        );
      })}
    </ButtonGroup>
  );
}

ChartSelector.propTypes = {
  onClick: PropTypes.func.isRequired,
};

export default ChartSelector;
