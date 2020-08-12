import React from 'react';
import PropTypes from 'prop-types';
import { Line } from 'react-chartjs-2';

import { url } from '../../../utils/url';
import useFetch from '../../../hook/useFetch';
import chartConfigStyles from './lineChartStyling';

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
    xAxes: [
      {
        ticks: {
          display: false,
        },
      },
    ],
  },
};

const LineChart = ({ config }) => {
  const csvData = useFetch({
    url: url.getDataUrl(config.dataSource),
    query: { columns: [config.xAxis, config.yAxis] },
  });

  const data = {
    labels: csvData && csvData.data[config.xAxis],
    datasets: [
      {
        ...chartConfigStyles.datasets[0],
        data: csvData && csvData.data[config.yAxis],
      },
    ],
  };

  return <Line data={data} options={options} />;
};

LineChart.displayName = 'LineChart';

LineChart.propTypes = {
  config: PropTypes.shape({
    dataSource: PropTypes.string.isRequired,
    xAxis: PropTypes.string.isRequired,
    yAxis: PropTypes.string.isRequired,
  }).isRequired,
};

export default React.memo(LineChart);
