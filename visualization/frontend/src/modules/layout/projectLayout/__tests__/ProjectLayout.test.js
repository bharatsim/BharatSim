import React, { useContext } from "react";
import { render } from "@testing-library/react";
import * as router from "react-router-dom";
import { api } from "../../../../utils/api";
import withThemeProvider from "../../../../theme/withThemeProvider";
import ProjectLayout from "../projectLayout/ProjectLayout";
import { projectLayoutContext } from "../../../../contexts/projectLayoutContext";

const mockHistoryPush = jest.fn();
const mockHistoryReplace = jest.fn();

function DummyComponent() {
  const { projectMetadata, selectedDashboardMetadata } = useContext(projectLayoutContext);
  return <div>{JSON.stringify({ projectMetadata, selectedDashboardMetadata }, null, 2)}</div>;
}

jest.mock('../../../../utils/api', () => ({
  api: {
    getProject: jest.fn().mockResolvedValue({ project: { name: 'name', _id: 'id' } }),
    getAllDashBoardByProjectId: jest
      .fn()
      .mockResolvedValue({ dashboards: [{ name: 'd_name', _id: 'd_id' }] }),
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

  it('should match snapshot for existed projects', async () => {
    router.useParams.mockReturnValue({ id: 1 });
    api.getProject.mockResolvedValue({ project: { _id: 1, name: 'project1' } });

    const { container, findByText } = render(<Component />);

    await findByText('project1');

    expect(container).toMatchSnapshot();
  });

  it('should create tabs for dashboard in sidebar panel', async () => {
    router.useParams.mockReturnValue({ id: 1 });
    api.getProject.mockResolvedValue({ project: { _id: 1, name: 'project1' } });

    const { findByText } = render(<Component />);
    await findByText('project1');

    expect(await findByText('d_name')).not.toBeNull();
  });
});
