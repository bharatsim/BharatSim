import React from 'react';
import { Line } from 'react-chartjs-2';
import {url} from "../../utils/url";
import useFetch from "../../hook/useFetch";

const options={ maintainAspectRatio: false, responsive: true }

function BasicLineChart(props,ref) {
  const {config = {}} = props;
  const csvData = useFetch({url:url.DATA, query: {columns: [config.xColumn, config.yColumn]}})

  const data = {
    labels: csvData && csvData.columns.hour,
    datasets: [
      {
        label: 'My First dataset',
        fill: false,
        lineTension: 0.1,
        backgroundColor: 'rgba(75,192,192,0.4)',
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
        data: csvData && csvData.columns.exposed,
      },
    ],
  };

  return (
      <Line ref={ref} data={data} options={options} />
  );
}

export default React.forwardRef(BasicLineChart)

