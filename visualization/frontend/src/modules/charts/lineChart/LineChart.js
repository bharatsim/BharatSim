import React from 'react';
import PropTypes from 'prop-types';
import { Line } from 'react-chartjs-2';
import { lineChartOptions } from '../chartStyleConfig';
import LoaderOrError from '../../../component/loaderOrError/LoaderOrError';
import useFetch from '../../../hook/useFetch';
import { api } from '../../../utils/api';
import { getYaxisNames, trasformDataForChart } from '../utils';

function LineChart({ config }) {
  const { xAxis: xColumn, yAxis, dataSource } = config;
  const yColumns = getYaxisNames(yAxis);

  const { data: csvData, loadingState } = useFetch(api.getData, [
    dataSource,
    [xColumn, ...yColumns],
  ]);

  const transformedData = trasformDataForChart(csvData, xColumn, yColumns);

  return (
    <LoaderOrError loadingState={loadingState}>
      <Line data={transformedData} options={lineChartOptions} />
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
