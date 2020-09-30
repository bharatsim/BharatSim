import React from 'react';
import { render } from '@testing-library/react';
import ConfigureDashboardData from '../ConfigureDashboardData';
import withThemeProvider from '../../../theme/withThemeProvider';

describe('Configure Dashboard Data', function () {
  const ConfigureDashboardDataWithTheme = withThemeProvider(ConfigureDashboardData);
  it('should render dashboard view for given dashboard data and project name ', function () {
    const { container } = render(
      <ConfigureDashboardDataWithTheme
        dashboardData={{ name: 'dashboard1' }}
        projectName="project1"
      />,
    );

    expect(container).toMatchSnapshot();
  });
});
