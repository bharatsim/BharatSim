import React from 'react';
import { fireEvent, render } from '@testing-library/react';

import DashboardNavbar from '../DashboardNavbar';

describe('Dashboard controller', function () {
  it('should match snapshot', function () {
    const handleChange = jest.fn();
    const { container } = render(
      <DashboardNavbar
        value={0}
        handleChange={handleChange}
        controllers={['dashboard1', 'dashboard2', 'dashboard3']}
      />,
    );
    expect(container).toMatchSnapshot();
  });
  it('should call handle change on click of tab', function () {
    const handleChange = jest.fn();
    const { getByTestId } = render(
      <DashboardNavbar
        value={0}
        handleChange={handleChange}
        controllers={['dashboard1', 'dashboard2', 'dashboard3']}
      />,
    );
    const dashboard1Tab = getByTestId('tab-dashboard1');

    fireEvent.click(dashboard1Tab);
    expect(handleChange).toHaveBeenCalled();
  });
});
