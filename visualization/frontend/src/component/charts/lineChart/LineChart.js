import React from 'react';
import PropTypes from 'prop-types';
import { Line } from 'react-chartjs-2';
import useFetch from '../../../hook/useFetch';
import chartConfigStyles from './lineChartStyling';
import { api } from '../../../utils/api';

const options = {
  maintainAspectRatio: false,
  responsive: true,
  animation: {
    duration: 0, // general animation time
  },
  hover: {
    animationDuration: 0, // duration of animations when hovering an item
  },
  responsiveAnimationDuration: 0,
  elements: {
    line: {
      tension: 0, // disables bezier curves
    },
  },
  scales: {
    yAxes: [
      {
        id: 'first-y-axis',
        type: 'linear',
        ticks: {
          sampleSize: 10,
          min: 0,
        },
      },
    ],
  },
};

function LineChart({ config }) {
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
        ...chartConfigStyles.datasets[0],
        data: csvData && csvData.data[yColumn],
      },
    ],
  };

  return <Line data={data} options={options} />;
}

LineChart.displayName = 'LineChart';

LineChart.propTypes = {
  config: PropTypes.shape({
    dataSource: PropTypes.string.isRequired,
    xAxis: PropTypes.string.isRequired,
    yAxis: PropTypes.shape({
      name: PropTypes.string.isRequired,
      type: PropTypes.string.isRequired,
    }).isRequired,
  }).isRequired,
};

export default React.memo(LineChart);
