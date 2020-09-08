import { render } from '@testing-library/react';
import React from 'react';

import LineChart from '../LineChart';
import useFetchAndTransformChartData from '../../../../hook/useFetchAndTransformChartData';

jest.mock('../../../../hook/useFetchAndTransformChartData');

describe('LineChart', () => {
  beforeEach(() => {
    useFetchAndTransformChartData.mockReturnValue({
      data: { labels: [1, 2], datasets: [{ data: [2, 3] }] },
      loadingState: 'SUCCESS',
    });
  });

  it('should create a line chart with single yaxis <LineChart /> component', () => {
    const { container } = render(
      <LineChart
        config={{
          dataSource: 'dataSource',
          xAxis: 'hour',
          yAxis: [{ type: 'number', name: 'exposed' }],
        }}
      />,
    );
    expect(container).toMatchSnapshot();
  });

  it('should show loader while fetching data <LineChart /> component', () => {
    useFetchAndTransformChartData.mockReturnValue({ data: undefined, loadingState: 'LOADING' });
    const { container } = render(
      <LineChart
        config={{
          dataSource: 'dataSource',
          xAxis: 'hour',
          yAxis: [{ type: 'number', name: 'exposed' }],
        }}
      />,
    );
    expect(container).toMatchSnapshot();
  });

  it('should show error after getting error while fetching <LineChart /> component', () => {
    useFetchAndTransformChartData.mockReturnValue({ data: 'error', loadingState: 'ERROR' });
    const { container } = render(
      <LineChart
        config={{
          dataSource: 'dataSource',
          xAxis: 'hour',
          yAxis: [{ type: 'number', name: 'exposed' }],
        }}
      />,
    );
    expect(container).toMatchSnapshot();
  });

  it('should create a line chart with multiple yaxis <LineChart /> component', () => {
    const { container } = render(
      <LineChart
        config={{
          dataSource: 'dataSource',
          xAxis: 'hour',
          yAxis: [
            { type: 'number', name: 'exposed' },
            { type: 'number', name: 'suseptible' },
          ],
        }}
      />,
    );
    expect(container).toMatchSnapshot();
  });
});
