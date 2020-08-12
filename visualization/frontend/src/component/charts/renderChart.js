import chartConfig from '../../config/chartConfig';

function renderChart(chartType, chartProps) {
  return chartConfig[chartType].chart(chartProps);
}

export default renderChart;
