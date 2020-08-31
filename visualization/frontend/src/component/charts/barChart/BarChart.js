import React from 'react';
import { Bar } from 'react-chartjs-2';
import PropTypes from 'prop-types';
import useFetch from '../../../hook/useFetch';
import { api } from '../../../utils/api';

const chartConfig = {
  datasets: [
    {
      label: 'Bar Chart',
      fill: false,
      lineTension: 0.1,
      backgroundColor: 'red',
      borderColor: 'rgba(75,192,192,1)',
      borderCapStyle: 'butt',
      borderDash: [],
      borderDashOffset: 0.0,
      borderJoinStyle: 'miter',
      pointBorderColor: 'rgba(75,192,192,1)',
      pointBackgroundColor: '#fff',
      pointBorderWidth: 1,
      pointHoverRadius: 5,
      pointHoverBackgroundColor: 'rgba(75,192,192,1)',
      pointHoverBorderColor: 'rgba(220,220,220,1)',
      pointHoverBorderWidth: 2,
      pointRadius: 1,
      pointHitRadius: 10,
    },
  ],
};

const options = { maintainAspectRatio: false, responsive: true };

function BarChart({ config }) {
  const {
    xAxis: xColumn,
    yAxis: { name: yColumn },
    dataSource,
  } = config;

  const csvData = useFetch(api.getData, {
    params: dataSource,
    query: { columns: [xColumn, yColumn] },
  });

  const data = {
    labels: csvData && csvData.data[xColumn],
    datasets: [
      {
        data: csvData && csvData.data[yColumn],
        ...chartConfig.datasets[0],
      },
    ],
  };

  return <Bar data={data} options={options} />;
}

BarChart.propTypes = {
  config: PropTypes.shape({
    dataSource: PropTypes.string.isRequired,
    xAxis: PropTypes.string.isRequired,
    yAxis: PropTypes.shape({
      name: PropTypes.string.isRequired,
      type: PropTypes.string.isRequired,
    }).isRequired,
  }).isRequired,
};

export default BarChart;
