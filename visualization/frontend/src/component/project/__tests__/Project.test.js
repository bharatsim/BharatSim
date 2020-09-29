import React from 'react';
import { render } from '@testing-library/react';
import { fireEvent, waitFor } from '@testing-library/dom';
import * as router from 'react-router-dom';
import { api } from '../../../utils/api';
import Project from '../Project';
import withThemeProvider from '../../../theme/withThemeProvider';

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

describe('Project', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });
  afterEach(() => {
    jest.clearAllMocks();
  });

  const Component = withThemeProvider(Project);

  it('should match snapshot while creating new project', async () => {
    router.useParams.mockReturnValue({ id: undefined });

    const { container, findByText } = render(<Component />);
    await findByText('untitled project');

    expect(container).toMatchSnapshot();
  });

  it('should match snapshot if project id is present', async () => {
    router.useParams.mockReturnValue({ id: 1 });
    api.getProject.mockResolvedValue({ projects: { _id: 1, name: 'project1' } });

    const { container, findByText } = render(<Component />);

    await findByText('project1');

    expect(container).toMatchSnapshot();
  });
  it('should show error components for failure while fetching project', async () => {
    router.useParams.mockReturnValue({ id: 1 });
    api.getProject.mockRejectedValue();

    const { findByText, queryByText } = render(<Component />);

    await findByText('Failed to load, Refresh the page');

    expect(queryByText('Failed to load, Refresh the page')).toBeInTheDocument();
  });
  it('should navigate to recent projects on click of back to recent button', async () => {
    router.useParams.mockReturnValueOnce({ id: 1 });
    api.getProject.mockResolvedValue({ projects: { _id: 1, name: 'project1' } });

    const { getByText, findByText } = render(<Component />);

    await findByText('project1');

    fireEvent.click(getByText('Back to recent projects'));

    expect(mockHistoryPush).toHaveBeenCalledWith('/');
  });

  it('should update the url on successful saving of project', async () => {
    api.saveProject.mockResolvedValue({ projectId: 1 });
    api.getProject.mockResolvedValue({ projects: { _id: 1, name: 'project1' } });
    router.useParams.mockReturnValue({ id: undefined });
    const { getByText } = render(<Component />);

    fireEvent.click(getByText('Save'));

    router.useParams.mockReturnValue({ id: 1 });

    await waitFor(() =>
      expect(mockHistoryReplace).toHaveBeenCalledWith({ pathname: '/project/1' }),
    );
  });

  it('should save project on click of save  button for old project', async () => {
    api.saveProject.mockResolvedValue({ projectId: 1 });
    api.getProject.mockResolvedValue({ projects: { _id: 1, name: 'project1' } });
    router.useParams.mockReturnValue({ id: 1 });
    const { getByText, findByText } = render(<Component />);

    await findByText('Save');
    fireEvent.click(getByText('Save'));

    expect(api.saveProject).toHaveBeenCalledWith({ id: 1, name: 'project1' });
  });
});
