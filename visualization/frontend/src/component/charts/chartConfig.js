const colors = [
  '#4dc9f6',
  '#f67019',
  '#f53794',
  '#537bc4',
  '#acc236',
  '#166a8f',
  '#00a950',
  '#58595b',
  '#8549ba',
];
function getColor(index) {
  return colors[index % colors.length];
}

const chartConfig = {
  fill: false,
  borderWidth: 1,
  pointBorderWidth: 1,
  pointRadius: 1,
  pointHitRadius: 10,
};

const lineChartOptions = {
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

export { chartConfig, getColor, lineChartOptions };
