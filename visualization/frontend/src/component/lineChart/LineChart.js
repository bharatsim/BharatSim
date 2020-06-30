import React from 'react';
import { Line } from 'react-chartjs-2';

import {url} from "../../utils/url";
import useFetch from "../../hook/useFetch";
import chartConfig from "./lineChartConfig";

const options={ maintainAspectRatio: false, responsive: true }

function BasicLineChart(props,ref) {
  const {config = {}} = props;
  const csvData = useFetch({url:url.DATA, query: {columns: [config.xColumn, config.yColumn]}})

  const data = {
    labels: csvData && csvData.columns.hour,
    datasets: [
      {
        ...chartConfig.datasets[0],
        data: csvData && csvData.columns.exposed,
      },
    ],
  };

  return (
      <Line ref={ref} data={data} options={options} />
  );
}

export default React.forwardRef(BasicLineChart)

