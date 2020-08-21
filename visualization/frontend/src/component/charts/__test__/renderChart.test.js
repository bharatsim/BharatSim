import React from 'react';
import { render } from '@testing-library/react';

import renderChart from '../renderChart';
import { chartTypes } from '../../../constants/charts';

jest.mock('../lineChart/LineChart', () => (props) => (
  <div>
    Line chart
    <span>
      {/* eslint-disable-next-line no-undef */}
      {mockPropsCapture(props)}
    </span>
  </div>
));

describe('Render chart config', () => {
  it('should render chart config with provided props', () => {
    const chartType = chartTypes.LINE_CHART;
    const chartProps = { config: { dataSource: 'test', xAxis: 'testA', yAxis: 'testB' } };
    const Chart = renderChart(chartType, chartProps);
    const { container } = render(Chart);

    expect(container).toMatchSnapshot();
  });
});
