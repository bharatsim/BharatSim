import React from 'react';
import { render } from '@testing-library/react';
import DashboardView from '../DashboardView';
import withThemeProvider from '../../../theme/withThemeProvider';

describe('Dashboard View', function () {
  const DashboardViewWithTheme = withThemeProvider(DashboardView);
  it('should render dashboard view for given dashboard data and project name ', function () {
    const { container } = render(
      <DashboardViewWithTheme
        value={0}
        dashboardData={[{ name: 'dashboard1' }, { name: 'dashboard2' }]}
        projectName="project1"
      />,
    );
    expect(container).toMatchSnapshot();
  });
});
