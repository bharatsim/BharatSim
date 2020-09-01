import React from 'react';
import { Bar } from 'react-chartjs-2';
import PropTypes from 'prop-types';
import useFetch from '../../../hook/useFetch';
import { api } from '../../../utils/api';
import { chartConfig, getColor } from '../chartConfig';

function getYaxisNames(yColumns) {
  return yColumns.map((yColumn) => yColumn.name);
}

const options = { maintainAspectRatio: false, responsive: true };

function BarChart({ config }) {
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
