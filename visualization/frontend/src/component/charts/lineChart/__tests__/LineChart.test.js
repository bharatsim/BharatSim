import { render } from '@testing-library/react';
import React from 'react';

import LineChart from '../LineChart';
import useFetch from '../../../../hook/useFetch';

jest.mock('../../../../hook/useFetch');

describe('LineChart', () => {
  beforeEach(() => {
    useFetch.mockReturnValue({ data: { exposed: [2, 3], hour: [1, 2] } });
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
