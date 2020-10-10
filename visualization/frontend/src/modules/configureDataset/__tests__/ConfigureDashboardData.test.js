import React from 'react';
import { render } from '@testing-library/react';
import { fireEvent } from '@testing-library/dom';
import { ConfigureDashboardData } from '../ConfigureDashboardData';
import withThemeProvider from '../../../theme/withThemeProvider';

const mockHistoryPush = jest.fn();
const mockHistoryReplace = jest.fn();

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useParams: jest.fn(),
  useHistory: () => ({
    push: mockHistoryPush,
    replace: mockHistoryReplace,
  }),
}));

describe('Configure Dashboard Data', function () {
  const ConfigureDashboardDataWithTheme = withThemeProvider(ConfigureDashboardData);
  it('should render dashboard view for given dashboard data and project name ', function () {
    const { container } = render(
      <ConfigureDashboardDataWithTheme
        dashboardData={{ name: 'dashboard1' }}
        projectMetadata={{ name: 'project1' }}
      />,
    );
    expect(container).toMatchSnapshot();
  });
  it('should navigate to recent projects on click of back to recent button', () => {
    const { getByText } = render(
      <ConfigureDashboardDataWithTheme
        dashboardData={{ name: 'dashboard1' }}
        projectMetadata={{ name: 'project1' }}
      />,
    );

    fireEvent.click(getByText('Back to recent projects'));

    expect(mockHistoryPush).toHaveBeenCalledWith('/');
  });
  it('should opne upload data screen on click of upload dataset link', () => {
    const { getByText } = render(
      <ConfigureDashboardDataWithTheme
        dashboardData={{ name: 'dashboard1' }}
        projectMetadata={{ name: 'project1', id: 'projectId' }}
      />,
    );
    fireEvent.click(getByText('Upload dataset'));

    expect(mockHistoryPush).toHaveBeenCalledWith('/projects/projectId/upload-dataset');
  });
});
