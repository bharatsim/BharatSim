import { render } from '@testing-library/react';
import React from 'react';

import LineChart from '../LineChart';
import useFetch from '../../../hook/useFetch';

jest.mock('../../../hook/useFetch');

describe('LineChart', () => {
  beforeEach(() => {
    useFetch.mockReturnValue({ data: { exposed: [2, 3], hour: [1, 2] } });
  });

  it('should have fetched text in <LineChart /> component', () => {
    const { container } = render(
      <LineChart config={{ dataSource: 'dataSource', xColumn: 'hour', yColumn: 'exposed' }} />,
    );
    expect(container).toMatchSnapshot();
  });
});