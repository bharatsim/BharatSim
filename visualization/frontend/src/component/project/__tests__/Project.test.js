import React from 'react';
import { render } from '@testing-library/react';
import { fireEvent } from '@testing-library/dom';
import * as router from 'react-router-dom';
import { api } from '../../../utils/api';
import Project from '../Project';
import withThemeProvider from '../../../theme/withThemeProvider';

const mockHistoryPush = jest.fn();

jest.mock('../../../utils/api', () => ({
  api: {
    getProject: jest.fn(),
    createNewProject: jest.fn(),
  },
}));

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useParams: jest.fn(),
  useHistory: () => ({
    push: mockHistoryPush,
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
    router.useParams.mockReturnValue({ id: 'createNew' });
    api.createNewProject.mockResolvedValue({ projectId: 1 });

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
  it('should navigate to recent projects on click of back to recent button', async () => {
    router.useParams.mockReturnValueOnce({ id: 1 });
    api.getProject.mockResolvedValue({ projects: { _id: 1, name: 'project1' } });

    const { getByText, findByText } = render(<Component />);

    await findByText('project1');

    fireEvent.click(getByText('Back to recent projects'));

    expect(mockHistoryPush).toHaveBeenCalledWith('/');
  });
});
