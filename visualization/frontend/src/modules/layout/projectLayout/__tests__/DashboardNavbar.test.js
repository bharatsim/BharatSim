import React from 'react';
import { render } from '@testing-library/react';
import SideDashboardNavbar from '../sideDashboardNavbar/SideDashboardNavbar';
import withThemeProvider from '../../../../theme/withThemeProvider';

describe('Dashboard Navbar', () => {
  const DashboardNavbarWithTheme = withThemeProvider(SideDashboardNavbar);
  it('should render dashboard view for given dashboard data and project name ', () => {
    const { container } = render(
      <DashboardNavbarWithTheme value={0} handleChange={() => {}} navItems={['dashboard1']} />,
    );

    expect(container).toMatchSnapshot();
  });
});
