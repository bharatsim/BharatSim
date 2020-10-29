import React from 'react';
import { Router } from 'react-router-dom';
import { render } from '@testing-library/react';
import { fireEvent } from '@testing-library/dom';
import { createMemoryHistory } from 'history';

import ConfigureDataset from '../ConfigureDataset';
import withThemeProvider from '../../../theme/withThemeProvider';
import { ProjectLayoutProvider } from '../../../contexts/projectLayoutContext';
import { api } from '../../../utils/api';

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

jest.mock('../../../utils/api', () => ({
  api: {
    getDatasources: jest.fn().mockResolvedValue({
      dataSources: [
        {
          createdAt: '2020-10-29T09:17:09.146Z',
          dashboardId: '5f9952ede93dbd234a39d82f',
          fileSize: 125005,
          fileType: 'text/csv',
          name: 'csv-file-name',
          updatedAt: '2020-10-29T09:17:09.146Z',
          _id: '5f9a88952629222105e180df',
        },
      ],
    }),
  },
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
  it('should match snapshot for given dashboard data and project name ', async () => {
    const { container, findByText } = render(<ComponentWithProvider />);

    await findByText('Configure Dashboard Data');

    expect(container).toMatchSnapshot();
  });

  it('should render configure dataset header with for given dashboard data and project name ', async () => {
    const { getByText, findByText } = render(<ComponentWithProvider />);

    await findByText('Configure Dashboard Data');

    expect(getByText('project1 :: dashboard1')).toBeInTheDocument();
  });

  it('should navigate to recent projects on click of back to recent button', async () => {
    const { getByText, findByText } = render(<ComponentWithProvider />);

    await findByText('Configure Dashboard Data');

    fireEvent.click(getByText('Back to recent projects'));

    expect(mockHistoryPush).toHaveBeenCalledWith('/');
  });

  it('should navigate to upload data screen on click of upload dataset button', async () => {
    const { getByText, findByText } = render(<ComponentWithProvider />);

    await findByText('Configure Dashboard Data');

    fireEvent.click(getByText('Upload Data'));

    expect(mockHistoryPush).toHaveBeenCalledWith('/projects/123/upload-dataset');
  });

  it('should display table of data sources if data sources are not empty', async () => {
    const { getByText, findByText } = render(<ComponentWithProvider />);

    await findByText('Configure Dashboard Data');

    expect(getByText('csv-file-name')).toBeInTheDocument();
  });

  it('should navigate to upload data screen on click of upload dataset link', async () => {
    api.getDatasources.mockResolvedValue({ dataSources: [] });
    const { getByText, findByText } = render(<ComponentWithProvider />);

    await findByText('Configure Dashboard Data');

    fireEvent.click(getByText('Upload dataset'));

    expect(history.location.pathname).toEqual('/projects/123/upload-dataset');
  });

  it('should display no datasource message if data sources are empty', async () => {
    api.getDatasources.mockResolvedValue({ dataSources: [] });
    const { getByText, findByText } = render(<ComponentWithProvider />);

    await findByText('Configure Dashboard Data');

    expect(
      getByText('Before we can create any visualization, we ‘ll need some data.'),
    ).toBeInTheDocument();
  });
});
