import chartConfigs from '../../config/chartConfigs';

function renderChart(chartType, chartProps) {
  return chartConfigs[chartType].chart(chartProps);
}

export default renderChart;
