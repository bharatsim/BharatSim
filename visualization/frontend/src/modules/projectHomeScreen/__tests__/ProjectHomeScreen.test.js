import React from "react";
import { fireEvent, render, within } from "@testing-library/react";
import ProjectHomeScreen from "../ProjectHomeScreen";
import withThemeProvider from "../../../theme/withThemeProvider";
import { api } from "../../../utils/api";

jest.mock('../../../utils/api', () => ({
  api: {
    saveProject: jest.fn().mockResolvedValue({ projectId: 'projectId' }),
    addNewDashboard: jest.fn().mockResolvedValue({}),
  },
}));

const mockHistoryPush = jest.fn();

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useHistory: () => ({
    push: mockHistoryPush,
  }),
}));

describe('<ProjectHomeScreenComponent />', () => {
  const ProjectHomeScreenComponent = withThemeProvider(ProjectHomeScreen);
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
    const { getByText, findByTestId } = render(<ProjectHomeScreenComponent />);
    fireEvent.click(getByText('Click here to create your first dashboard.'));

    const container = within(document.querySelector('.MuiPaper-root'));

    fireEvent.change(container.getByLabelText('Project Title'), {
      target: { value: 'projectName' },
    });
    fireEvent.click(container.getByText('create'));

    await findByTestId('loader');


    expect(api.saveProject).toHaveBeenCalledWith({ name: 'projectName' });
  });

  it('should call create dashboard api after click on create button with inputted data and projectId ', async () => {
    const { getByText, findByTestId } = render(<ProjectHomeScreenComponent />);
    fireEvent.click(getByText('Click here to create your first dashboard.'));

    const container = within(document.querySelector('.MuiPaper-root'));

    fireEvent.change(container.getByLabelText('Dashboard Title'), {
      target: { value: 'DashboardName' },
    });
    fireEvent.click(container.getByText('create'));

    await findByTestId('loader');


    expect(api.addNewDashboard).toHaveBeenCalledWith({
      name: 'DashboardName',
      projectId: 'projectId',
    });
  });

  it('should navigate to project page', async () => {
    const { getByText, findByTestId } = render(<ProjectHomeScreenComponent />);
    fireEvent.click(getByText('Click here to create your first dashboard.'));

    const container = within(document.querySelector('.MuiPaper-root'));

    fireEvent.change(container.getByLabelText('Dashboard Title'), {
      target: { value: 'DashboardName' },
    });
    fireEvent.click(container.getByText('create'));

    await findByTestId('loader');

    expect(mockHistoryPush).toHaveBeenCalledWith("/projects/projectId");
  });
});
