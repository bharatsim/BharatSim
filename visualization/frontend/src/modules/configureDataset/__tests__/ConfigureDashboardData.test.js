import React from 'react';
import { render } from '@testing-library/react';
import { fireEvent } from '@testing-library/dom';
import ConfigureDashboardData from '../ConfigureDashboardData';
import withThemeProvider from '../../../theme/withThemeProvider';
import { ProjectLayoutProvider } from '../../../contexts/projectLayoutContext';

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

const ComponentWithProvider = withThemeProvider(() => (
  <ProjectLayoutProvider
    value={{
      projectMetadata: { name: 'project1', id: '123' },
      selectedDashboardMetadata: { name: 'dashboard1' },
    }}
  >
    <ConfigureDashboardData />
  </ProjectLayoutProvider>
));

describe('Configure Dashboard Data', () => {
  it('should match snapshot for given dashboard data and project name ', () => {
    const { container } = render(<ComponentWithProvider />);

    expect(container).toMatchSnapshot();
  });

  it('should render configure dataset header with for given dashboard data and project name ', () => {
    const { getByText } = render(<ComponentWithProvider />);

    expect(getByText('project1 :: dashboard1')).toBeInTheDocument();
  });

  it('should navigate to recent projects on click of back to recent button', () => {
    const { getByText } = render(<ComponentWithProvider />);

    fireEvent.click(getByText('Back to recent projects'));

    expect(mockHistoryPush).toHaveBeenCalledWith('/');
  });
  it('should opne upload data screen on click of upload dataset link', () => {
    const { getByText } = render(<ComponentWithProvider />);

    fireEvent.click(getByText('Upload dataset'));

    expect(mockHistoryPush).toHaveBeenCalledWith('/projects/123/upload-dataset');
  });
});
