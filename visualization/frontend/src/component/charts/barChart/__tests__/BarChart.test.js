import { render } from '@testing-library/react';
import React from 'react';

import BarChart from '../BarChart';
import useFetchAndTransformChartData from '../../../../hook/useFetchAndTransformChartData';

jest.mock('../../../../hook/useFetchAndTransformChartData');

describe('BarChart', () => {
  beforeEach(() => {
    useFetchAndTransformChartData.mockReturnValue({
      data: { labels: [1, 2], datasets: [{ data: [2, 3] }] },
      loadingState: 'SUCCESS',
    });
  });

  it('should have fetched text in <BarChart /> component', () => {
    const { container } = render(
      <BarChart
        config={{
          dataSource: 'dataSource',
          xAxis: 'hour',
          yAxis: [{ type: 'number', name: 'exposed' }],
        }}
      />,
    );
    expect(container).toMatchSnapshot();
  });

  it('should show error after getting error while fetching <BarChart /> component', () => {
    useFetchAndTransformChartData.mockReturnValue({
      data: undefined,
      loadingState: 'ERROR',
    });
    const { container } = render(
      <BarChart
        config={{
          dataSource: 'dataSource',
          xAxis: 'hour',
          yAxis: [{ type: 'number', name: 'exposed' }],
        }}
      />,
    );
    expect(container).toMatchSnapshot();
  });

  it('should show loader while fetching data <BarChart /> component', () => {
    useFetchAndTransformChartData.mockReturnValue({
      data: undefined,
      loadingState: 'LOADING',
    });
    const { container } = render(
      <BarChart
        config={{
          dataSource: 'dataSource',
          xAxis: 'hour',
          yAxis: [{ type: 'number', name: 'exposed' }],
        }}
      />,
    );
    expect(container).toMatchSnapshot();
  });
});
