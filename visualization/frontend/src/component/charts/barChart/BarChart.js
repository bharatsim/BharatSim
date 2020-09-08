import React from 'react';
import { Bar } from 'react-chartjs-2';
import PropTypes from 'prop-types';
import useFetchAndTransformChartData from '../../../hook/useFetchAndTransformChartData';
import LoaderOrError from '../../loaderOrError/LoaderOrError';

const options = { maintainAspectRatio: false, responsive: true };

function BarChart({ config }) {
  const { data, loadingState } = useFetchAndTransformChartData(config);
  return (
    <LoaderOrError loadingState={loadingState}>
      <Bar data={data} options={options} />
    </LoaderOrError>
  );
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
