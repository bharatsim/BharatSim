import React from 'react';
import { Bar } from 'react-chartjs-2';
import PropTypes from 'prop-types';
import useFetchAndTransformChartData from '../../../hook/useFetchAndTransformChartData';

const options = { maintainAspectRatio: false, responsive: true };

function BarChart({ config }) {
  const data = useFetchAndTransformChartData(config);
  return <Bar data={data} options={options} />;
}

BarChart.propTypes = {
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

export default BarChart;
