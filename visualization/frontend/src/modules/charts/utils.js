import { chartStyleConfig, getColor } from './chartStyleConfig';

function trasformDataForChart(csvData, xColumn, yColumns) {
  return {
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
}

function getYaxisNames(yColumns) {
  return yColumns.map((yColumn) => yColumn.name);
}

export { trasformDataForChart, getYaxisNames };
