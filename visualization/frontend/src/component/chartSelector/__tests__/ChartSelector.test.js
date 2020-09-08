import React from 'react';
import { fireEvent } from '@testing-library/dom';

import { render } from '@testing-library/react';
import ChartSelector from '../ChartSelector';

describe('<ChartSelector />', () => {
  it('should match snapshot', () => {
    const { container } = render(<ChartSelector onClick={jest.fn()} />);

    expect(container).toMatchSnapshot();
  });

  it('should call onclick callback on click of Line Chart button', () => {
    const onClick = jest.fn();
    const { getByText } = render(<ChartSelector onClick={onClick} />);

    const lineChartButton = getByText(/Line Chart/);

    fireEvent.click(lineChartButton);

    expect(onClick).toHaveBeenCalledWith('lineChart');
  });

  it('should call onclick callback on click of Bar Chart button', () => {
    const onClick = jest.fn();
    const { getByText } = render(<ChartSelector onClick={onClick} />);

    const lineChartButton = getByText(/Bar Chart/);

    fireEvent.click(lineChartButton);

    expect(onClick).toHaveBeenCalledWith('barChart');
  });
});
