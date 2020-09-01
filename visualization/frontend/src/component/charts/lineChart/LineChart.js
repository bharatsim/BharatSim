import React from 'react';
import PropTypes from 'prop-types';
import { Line } from 'react-chartjs-2';
import useFetch from '../../../hook/useFetch';
import { chartConfig, getColor, lineChartOptions } from '../chartConfig';
import { api } from '../../../utils/api';

function getYaxisNames(yColumns) {
  return yColumns.map((yColumn) => yColumn.name);
}
function LineChart({ config }) {
  const { xAxis: xColumn, yAxis, dataSource } = config;
  const yColumns = getYaxisNames(yAxis);
  const csvData = useFetch(api.getData, {
    params: dataSource,
    query: { columns: [xColumn, ...yColumns] },
  });

  const data = {
    labels: csvData && csvData.data[xColumn],
    datasets:
      csvData &&
      yColumns.map((yColumn, index) => {
        return {
          ...chartConfig.datasets[0],
          label: yColumn,
          borderColor: getColor(index),
          backgroundColor: getColor(index),
          data: csvData && csvData.data[yColumn],
        };
      }),
  };

  return <Line data={data} options={lineChartOptions} />;
}

LineChart.displayName = 'LineChart';

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
