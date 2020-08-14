import React from 'react';
import { render } from '@testing-library/react';
import { selectDropDownOption } from '../../../testUtil';
import YAxisChartConfig from '../YAxisChartConfig';

describe('<YAxisChartConfig />', () => {
  const props = {
    headers: [
      { name: 'a', type: 'number' },
      { name: 'b', type: 'number' },
      { name: 'c', type: 'number' },
    ],
    updateConfigState: jest.fn(),
    configKey: 'yAxis',
    error: '',
  };
  it('should match snapshot', () => {
    const { container } = render(<YAxisChartConfig {...props} />);

    expect(container).toMatchSnapshot();
  });

  it('should call setConfig callback after value change', () => {
    const renderedContainer = render(<YAxisChartConfig {...props} />);

    selectDropDownOption(renderedContainer, 'dropdown-y', 'a');

    expect(props.updateConfigState).toHaveBeenCalledWith('yAxis', { name: 'a', type: 'number' });
  });
});
