import React, { useContext } from 'react';
import { render } from '@testing-library/react';
import { fireEvent, waitFor } from '@testing-library/dom';
import * as router from 'react-router-dom';
import { api } from '../../../../utils/api';
import withThemeProvider from '../../../../theme/withThemeProvider';
import ProjectLayout from '../projectLayout/ProjectLayout';
import { projectLayoutContext } from '../../../../contexts/projectLayoutContext';

const mockHistoryPush = jest.fn();
const mockHistoryReplace = jest.fn();

function DummyComponent() {
  const { projectMetadata, selectedDashboardMetadata } = useContext(projectLayoutContext);
  return <div>{JSON.stringify({ projectMetadata, selectedDashboardMetadata }, null, 2)}</div>;
}

jest.mock('../../../../utils/api', () => ({
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
  afterEach(() => {
    jest.clearAllMocks();
  });

  const Component = withThemeProvider(() => (
    <ProjectLayout>
      <DummyComponent />
    </ProjectLayout>
  ));

  it('should match snapshot while creating new project', async () => {
    router.useParams.mockReturnValue({ id: undefined });

    const { container, findByText } = render(<Component />);

    await findByText('untitled project');

    expect(container).toMatchSnapshot();
  });

  it('should match snapshot for old projects', async () => {
    router.useParams.mockReturnValue({ id: 1 });
    api.getProject.mockResolvedValue({ project: { _id: 1, name: 'project1' } });

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

  it('should update the url on successful saving of project', async () => {
    router.useParams.mockReturnValue({ id: undefined });
    api.saveProject.mockResolvedValue({ projectId: 1 });
    api.getProject.mockResolvedValue({ project: { _id: 1, name: 'project1' } });

    const { getByText, findByText } = render(<Component />);

    await findByText('untitled project');

    fireEvent.click(getByText('Save'));

    router.useParams.mockReturnValue({ id: 1 });

    await waitFor(() =>
      expect(mockHistoryReplace).toHaveBeenCalledWith({ pathname: '/projects/1' }),
    );
  });

  it('should save project on click of save  button for old project', async () => {
    api.saveProject.mockResolvedValue({ projectId: 1 });
    api.getProject.mockResolvedValue({ project: { _id: 1, name: 'project1' } });
    router.useParams.mockReturnValue({ id: 1 });
    const { getByText, findByText } = render(<Component />);

    await findByText('Save');
    fireEvent.click(getByText('Save'));

    expect(api.saveProject).toHaveBeenCalledWith({ id: 1, name: 'project1' });
  });
});
