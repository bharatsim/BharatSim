import React from 'react';
import { fireEvent, render } from '@testing-library/react';
import Project from '../Project';

describe('Project', () => {
  it('should match snapshot', function () {
    const { container } = render(<Project />);
    expect(container).toMatchSnapshot();
  });
  it('should update selected dashboard on click of dashboard name', function () {
    const { getByTestId, getByText } = render(<Project />);
    const dashboard1Tab = getByTestId('tab-Dashboard 1');
    fireEvent.click(dashboard1Tab);
    const dashboard1View = getByText('dashboard1');
    expect(dashboard1View).not.toBeNull();
  });
});
