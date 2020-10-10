import React from 'react';
import PropTypes from 'prop-types';
import { Line } from 'react-chartjs-2';
import { lineChartOptions } from '../chartStyleConfig';
import useFetchAndTransformChartData from '../../../hook/useFetchAndTransformChartData';
import LoaderOrError from '../../../component/loaderOrError/LoaderOrError';

function LineChart({ config }) {
  const { data, loadingState } = useFetchAndTransformChartData(config);
  return (
    <LoaderOrError loadingState={loadingState}>
      <Line data={data} options={lineChartOptions} />
    </LoaderOrError>
  );
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
