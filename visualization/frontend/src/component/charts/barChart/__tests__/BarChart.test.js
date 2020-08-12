import { render } from '@testing-library/react';
import React from 'react';

import BarChart from '../BarChart';
import useFetch from '../../../../hook/useFetch';

jest.mock('../../../../hook/useFetch');

describe('BarChart', () => {
  beforeEach(() => {
    useFetch.mockReturnValue({ data: { exposed: [2, 3], hour: [1, 2] } });
  });

  it('should have fetched text in <BarChart /> component', () => {
    const { container } = render(
      <BarChart config={{ dataSource: 'dataSource', xAxis: 'hour', yAxis: 'exposed' }} />,
    );
    expect(container).toMatchSnapshot();
  });
});
