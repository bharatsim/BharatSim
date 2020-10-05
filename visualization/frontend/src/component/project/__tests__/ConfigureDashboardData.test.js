import React from 'react';
import { render } from '@testing-library/react';
import { fireEvent } from '@testing-library/dom';
import * as router from 'react-router-dom';
import ConfigureDashboardData from '../ConfigureDashboardData';
import withThemeProvider from '../../../theme/withThemeProvider';
import { api } from '../../../utils/api';

const mockHistoryPush = jest.fn();
const mockHistoryReplace = jest.fn();

jest.mock('../../../utils/api', () => ({
  api: {
    getProject: jest.fn(),
    saveProject: jest.fn(),
  },
}));

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
  it('should render dashboard view for given dashboard data and project name ', async function () {
    router.useParams.mockReturnValue({ id: 1 });
    api.getProject.mockResolvedValue({ project: { _id: 1, name: 'project1' } });
    const { container, findByText } = render(<ConfigureDashboardDataWithTheme />);
    await findByText('project1 :: dashboard1');
    expect(container).toMatchSnapshot();
  });
  it('should navigate to recent projects on click of back to recent button', async () => {
    router.useParams.mockReturnValueOnce({ id: 1 });
    api.getProject.mockResolvedValue({ project: { _id: 1, name: 'project1' } });

    const { getByText, findByText } = render(<ConfigureDashboardDataWithTheme />);

    await findByText('project1');

    fireEvent.click(getByText('Back to recent projects'));

    expect(mockHistoryPush).toHaveBeenCalledWith('/');
  });
});
