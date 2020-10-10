import useFetch from './useFetch';
import { api } from '../utils/api';
import { chartStyleConfig, getColor } from '../modules/charts/chartStyleConfig';

function getYaxisNames(yColumns) {
  return yColumns.map((yColumn) => yColumn.name);
}

export default function useFetchAndTransformChartData(config) {
  const { xAxis: xColumn, yAxis, dataSource } = config;
  const yColumns = getYaxisNames(yAxis);

  const { data: csvData, loadingState } = useFetch(api.getData, {
    params: dataSource,
    query: { columns: [xColumn, ...yColumns] },
  });

  const data = {
    labels: csvData && csvData.data[xColumn],
    datasets:
      csvData &&
      yColumns.map((yColumn, index) => {
        return {
          ...chartStyleConfig,
          label: yColumn,
          borderColor: getColor(index),
          backgroundColor: getColor(index),
          data: csvData && csvData.data[yColumn],
        };
      }),
  };
  return { data, loadingState };
}
