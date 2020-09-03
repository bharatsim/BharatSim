import React from 'react';
import PropTypes from 'prop-types';
import { Line } from 'react-chartjs-2';
import { lineChartOptions } from '../chartStyleConfig';
import useFetchAndTransformChartData from '../../../hook/useFetchAndTransformChartData';

function LineChart({ config }) {
  const data = useFetchAndTransformChartData(config);
  return <Line data={data} options={lineChartOptions} />;
}

LineChart.propTypes = {
  config: PropTypes.shape({
    dataSource: PropTypes.string.isRequired,
    xAxis: PropTypes.string.isRequired,
    yAxis: PropTypes.arrayOf(
      PropTypes.shape({
        name: PropTypes.string.isRequired,
        type: PropTypes.string.isRequired,
      }),
    ).isRequired,
  }).isRequired,
};

export default React.memo(LineChart);
