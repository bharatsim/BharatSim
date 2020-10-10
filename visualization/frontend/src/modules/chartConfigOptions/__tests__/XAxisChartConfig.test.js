import React from 'react';
import { render } from '@testing-library/react';

import XAxisChartConfig from '../XAxisChartConfig';
import { selectDropDownOption } from '../../../testUtil';

describe('<XAxisChartConfig />', () => {
  const props = {
    headers: [
      { name: 'a', type: 'number' },
      { name: 'b', type: 'number' },
      { name: 'c', type: 'number' },
    ],
    updateConfigState: jest.fn(),
    configKey: 'xAxis',
    error: '',
    value: '',
  };
  it('should match snapshot', () => {
    const { container } = render(<XAxisChartConfig {...props} />);

    expect(container).toMatchSnapshot();
  });

  it('should call setConfig callback after value change', () => {
    const renderedContainer = render(<XAxisChartConfig {...props} />);

    selectDropDownOption(renderedContainer, 'dropdown-x', 'a');

    expect(props.updateConfigState).toHaveBeenCalledWith('xAxis', 'a');
  });
});
