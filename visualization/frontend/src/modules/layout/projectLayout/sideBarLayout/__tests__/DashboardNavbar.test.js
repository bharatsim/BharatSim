import React from 'react';
import { render } from '@testing-library/react';
import DashboardNavbar from '../DashboardNavbar';
import withThemeProvider from '../../../../../theme/withThemeProvider';

describe('Dashboard Navbar', function () {
  const DashboardNavbarWithTheme = withThemeProvider(DashboardNavbar);
  it('should render dashboard view for given dashboard data and project name ', function () {
    const { container } = render(
      <DashboardNavbarWithTheme value={0} handleChange={() => {}} navItems={['dashboard1']} />,
    );

    expect(container).toMatchSnapshot();
  });
});
