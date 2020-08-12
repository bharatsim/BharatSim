import React from 'react';
import { render } from '@testing-library/react';

import XAxisChartConfig from '../XAxisChartConfig';
import { selectDropDownOption } from '../../../testUtil';

describe('<XAxisChartConfig />', () => {
  const props = {
    headers: ['a', 'b', 'c'],
    updateConfigState: jest.fn(),
    configKey: 'xAxis',
  };
  it('should match snapshot', () => {
    const { container } = render(<XAxisChartConfig {...props} />);

    expect(container).toMatchSnapshot();
  });

  it('should call setConfig callback after value change', () => {
    const renderedContainer = render(<XAxisChartConfig {...props} />);

    selectDropDownOption(renderedContainer, 'dropdown-x', 'a');

    expect(props.updateConfigState).toHaveBeenCalledWith({ xAxis: 'a' });
  });
});
