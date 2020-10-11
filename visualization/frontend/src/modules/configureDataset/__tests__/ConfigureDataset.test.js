import React from 'react';
import { Router } from 'react-router-dom';
import { render } from '@testing-library/react';
import { fireEvent } from '@testing-library/dom';
import { createMemoryHistory } from 'history';

import ConfigureDataset from '../ConfigureDataset';
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

const history = createMemoryHistory();

const ComponentWithProvider = withThemeProvider(() => (
  <Router history={history}>
    <ProjectLayoutProvider
      value={{
        projectMetadata: { name: 'project1', id: '123' },
        selectedDashboardMetadata: { name: 'dashboard1' },
      }}
    >
      <ConfigureDataset />
    </ProjectLayoutProvider>
  </Router>
));

describe('Configure datasets', () => {
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

  it('should navigate to upload data screen on click of upload dataset link', () => {
    const { getByText } = render(<ComponentWithProvider />);

    fireEvent.click(getByText('Upload dataset'));

    expect(history.location.pathname).toEqual('/projects/123/upload-dataset');
  });
});
