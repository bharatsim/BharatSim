import React from 'react';
import { fireEvent, render, within } from '@testing-library/react';
import { SnackbarProvider } from 'notistack';
import ProjectHomeScreen from '../ProjectHomeScreen';
import withThemeProvider from '../../../theme/withThemeProvider';
import { api } from '../../../utils/api';
import { ProjectLayoutProvider } from '../../../contexts/projectLayoutContext';

jest.mock('../../../utils/api', () => ({
  api: {
    saveProject: jest.fn().mockResolvedValue({ projectId: 'projectId' }),
    addNewDashboard: jest.fn().mockResolvedValue({ _id: 'dashboardId' }),
  },
}));

const mockHistoryPush = jest.fn();
const mockHistoryReplace = jest.fn();

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useHistory: () => ({
    push: mockHistoryPush,
    replace: mockHistoryReplace,
  }),
}));

function openFillAndSubmitNewProjectForm(renderedComponent) {
  const { getByText } = renderedComponent;
  fireEvent.click(getByText('Click here to create your first dashboard.'));

  const container = within(document.querySelector('.MuiPaper-root'));

  fireEvent.change(container.getByLabelText('Dashboard Title'), {
    target: { value: 'DashboardName' },
  });
  fireEvent.change(container.getByLabelText('Project Title'), {
    target: { value: 'ProjectName' },
  });

  fireEvent.click(container.getByText('create'));
}

describe('<ProjectHomeScreenComponent />', () => {
  let ProjectHomeScreenComponent;
  beforeEach(() => {
    ProjectHomeScreenComponent = withThemeProvider(() => (
      <SnackbarProvider>
        <ProjectLayoutProvider
          value={{
            projectMetadata: {
              name: '',
            },
            selectedDashboardMetadata: {
              name: '',
            },
          }}
        >
          <ProjectHomeScreen />
        </ProjectLayoutProvider>
      </SnackbarProvider>
    ));
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('should match snapshot', () => {
    const { container } = render(<ProjectHomeScreenComponent />);

    expect(container).toMatchSnapshot();
  });

  it('should open new dashboard popup onclick of card', () => {
    const { getByText } = render(<ProjectHomeScreenComponent />);
    fireEvent.click(getByText('Click here to create your first dashboard.'));

    const container = within(document.querySelector('.MuiPaper-root'));

    expect(container.queryByText('New Dashboard')).toBeInTheDocument();
  });

  it('should call create project api after click on create button with inputted data', async () => {
    const renderComponent = render(<ProjectHomeScreenComponent />);

    openFillAndSubmitNewProjectForm(renderComponent);

    await renderComponent.findByText('Project ProjectName and Dashboard DashboardName are saved');

    expect(api.saveProject).toHaveBeenCalledWith({ name: 'ProjectName' });
  });

  it('should call create project api and dashboard api after click on create button with default data', async () => {
    const { getByText, findByText } = render(<ProjectHomeScreenComponent />);

    fireEvent.click(getByText('Click here to create your first dashboard.'));

    const container = within(document.querySelector('.MuiPaper-root'));

    fireEvent.click(container.getByText('create'));

    await findByText('Project Untitled Project and Dashboard Untitled Dashboard are saved');

    expect(api.saveProject).toHaveBeenCalledWith({ name: 'Untitled Project' });
    expect(api.addNewDashboard).toHaveBeenCalledWith({
      name: 'Untitled Dashboard',
      projectId: 'projectId',
    });
  });

  it('should call create dashboard api after click on create button with inputted data and projectId ', async () => {
    const renderComponent = render(<ProjectHomeScreenComponent />);

    openFillAndSubmitNewProjectForm(renderComponent);

    await renderComponent.findByText('Project ProjectName and Dashboard DashboardName are saved');

    expect(api.addNewDashboard).toHaveBeenCalledWith({
      name: 'DashboardName',
      projectId: 'projectId',
    });
  });

  it('should navigate to project page', async () => {
    const renderComponent = render(<ProjectHomeScreenComponent />);

    openFillAndSubmitNewProjectForm(renderComponent);

    await renderComponent.findByText('Project ProjectName and Dashboard DashboardName are saved');

    expect(mockHistoryReplace).toHaveBeenCalledWith({
      pathname: '/projects/projectId/configure-dataset',
    });
  });

  it('should display snackbar for successful message for dashboard and project creation', async () => {
    const renderComponent = render(<ProjectHomeScreenComponent />);

    openFillAndSubmitNewProjectForm(renderComponent);

    const component = await renderComponent.findByText(
      'Project ProjectName and Dashboard DashboardName are saved',
    );

    expect(component).toBeInTheDocument();
  });

  it('should display snackbar for error message for dashboard creation failed', async () => {
    api.addNewDashboard.mockRejectedValue('error');
    const renderComponent = render(<ProjectHomeScreenComponent />);

    openFillAndSubmitNewProjectForm(renderComponent);

    const component = await renderComponent.findByText(
      'Error while saving Dashboard DashboardName',
    );

    expect(component).toBeInTheDocument();
  });

  it('should display snackbar for success message of only successful project creation', async () => {
    api.addNewDashboard.mockRejectedValue('error');
    const renderComponent = render(<ProjectHomeScreenComponent />);

    openFillAndSubmitNewProjectForm(renderComponent);

    const component = await renderComponent.findByText('Project ProjectName is saved');

    expect(component).toBeInTheDocument();
  });

  it('should display errormessage of failure project creation and dashboard creation', async () => {
    api.saveProject.mockRejectedValue('error');
    api.addNewDashboard.mockRejectedValue('error');
    const renderComponent = render(<ProjectHomeScreenComponent />);

    openFillAndSubmitNewProjectForm(renderComponent);

    const component = await renderComponent.findByText(
      'Error while saving Project ProjectName and Dashboard DashboardName',
    );

    expect(component).toBeInTheDocument();
  });
});
