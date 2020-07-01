import { render } from '@testing-library/react';
import React from 'react';

import LineChart from '../LineChart';
import useFetch from '../../../hook/useFetch';

jest.mock('../../../hook/useFetch');

describe('LineChart', () => {
  beforeEach(() => {
    useFetch.mockReturnValue({ columns: { exposed: [2, 3], hour: [1, 2] } });
  });

  it('should have fetched text in <LineChart /> component', () => {
    const { container } = render(<LineChart config={{ xColumn: 'hours', yColumn: 'exposed' }} />);
    expect(container).toMatchSnapshot();
  });
});
