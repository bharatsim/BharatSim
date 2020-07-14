import React, { forwardRef } from 'react';
import PropTypes from 'prop-types';
import { Line } from 'react-chartjs-2';

import { url } from '../../utils/url';
import useFetch from '../../hook/useFetch';
import chartConfig from './lineChartConfig';

const options = { maintainAspectRatio: false, responsive: true };

const BasicLineChart = forwardRef(({ config }, ref) => {
  const csvData = useFetch({
    url: url.getDataUrl(config.dataSource),
    query: { columns: [config.xColumn, config.yColumn] },
  });

  const data = {
    labels: csvData && csvData.data[config.xColumn],
    datasets: [
      {
        ...chartConfig.datasets[0],
        data: csvData && csvData.data[config.yColumn],
      },
    ],
  };

  return <Line ref={ref} data={data} options={options} />;
});

BasicLineChart.displayName = 'LineChart';

BasicLineChart.propTypes = {
  config: PropTypes.shape({
    dataSource: PropTypes.string.isRequired,
    xColumn: PropTypes.string.isRequired,
    yColumn: PropTypes.string.isRequired,
  }).isRequired,
};

export default BasicLineChart;
