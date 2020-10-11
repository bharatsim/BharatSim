import { render, waitFor } from '@testing-library/react';
import React from 'react';

import LineChart from '../LineChart';
import { api } from '../../../../utils/api';

jest.mock('../../../../utils/api', () => ({
  api: {
    getData: jest.fn().mockResolvedValue({
      data: { data: { exposed: [2, 3], hour: [1, 2] } },
    }),
  },
}));

describe('LineChart', () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  it('should create a line chart with single yaxis <LineChart /> component', async () => {
    const { container } = render(
      <LineChart
        config={{
          dataSource: 'dataSource',
          xAxis: 'hour',
          yAxis: [{ type: 'number', name: 'exposed' }],
        }}
      />,
    );

    await waitFor(() => document.getElementsByTagName('canvas'));

    expect(container).toMatchSnapshot();
  });

  it('should call get data api for given data column and datasource', async () => {
    render(
      <LineChart
        config={{
          dataSource: 'dataSource',
          xAxis: 'hour',
          yAxis: [{ type: 'number', name: 'exposed' }],
        }}
      />,
    );

    await waitFor(() => document.getElementsByTagName('canvas'));

    expect(api.getData).toHaveBeenCalledWith('dataSource', ['hour', 'exposed']);
  });
});
