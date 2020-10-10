import React from 'react';
import { render } from '@testing-library/react';
import { selectDropDownOption, selectDropDownOptionForMultiselect } from '../../../testUtil';
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
  afterEach(() => {
    jest.clearAllMocks();
  });
  it('should match snapshot', () => {
    const { container } = render(<YAxisChartConfig {...props} />);

    expect(container).toMatchSnapshot();
  });

  it('should call setConfig callback after value change', () => {
    const renderedContainer = render(<YAxisChartConfig {...props} />);

    selectDropDownOption(renderedContainer, 'dropdown-y', 'a');

    expect(props.updateConfigState).toHaveBeenCalledWith('yAxis', [{ name: 'a', type: 'number' }]);
  });
  it('should call setConfig callback after value change for multiple axis', () => {
    const renderedContainer = render(<YAxisChartConfig {...props} />);

    selectDropDownOptionForMultiselect(renderedContainer, 'dropdown-y', ['a', 'b']);

    expect(props.updateConfigState).toHaveBeenCalledWith('yAxis', [{ name: 'a', type: 'number' }]);
    expect(props.updateConfigState).toHaveBeenCalledWith('yAxis', [{ name: 'b', type: 'number' }]);
    expect(props.updateConfigState).toHaveBeenCalledTimes(2);
  });
});
