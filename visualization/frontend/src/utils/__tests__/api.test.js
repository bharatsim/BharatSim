import { api } from '../api';
import { fetchData, uploadData } from '../fetch';

jest.mock('../fetch', () => ({
  fetchData: jest.fn(),
  uploadData: jest.fn(),
}));

describe('API', () => {
  afterEach(() => {
    jest.clearAllMocks();
  });
  it('should call save dashboard api with provided data', () => {
    const data = { widgets: [], layout: [], dashboardId: 'id', name: 'name', count: 0 };

    const expectedParameter = {
      data: JSON.stringify({
        dashboardData: { widgets: [], layout: [], dashboardId: 'id', name: 'name', count: 0 },
      }),
      headers: { 'content-type': 'application/json' },
      url: '/api/dashboard',
    };

    api.saveDashboard(data);

    expect(uploadData).toHaveBeenCalledWith(expectedParameter);
  });

  it('should call dashboard api to get all dashboard', () => {
    const expectedParameter = {
      url: '/api/dashboard',
    };

    api.getAllDashBoard();

    expect(fetchData).toHaveBeenCalledWith(expectedParameter);
  });

  it('should call dashboard api to get all dashboard filter by project id with projected columns', () => {
    const expectedParameter = {
      url: '/api/dashboard',
      query: {
        columns: ['name', '_id'],
        projectId: 'projectId',
      },
    };

    api.getAllDashBoardByProjectId('projectId');

    expect(fetchData).toHaveBeenCalledWith(expectedParameter);
  });

  it('should call dashboard api to insert new dashboard', () => {
    const expectedParameter = {
      data: JSON.stringify({
        dashboardData: {
          widgets: [],
          layout: [],
          name: 'dashbaord1',
          count: 0,
          projectId: 'projectId',
        },
      }),
      headers: {
        'content-type': 'application/json',
      },
      url: '/api/dashboard/create-new',
    };

    api.addNewDashboard({ name: 'dashbaord1', projectId: 'projectId' });

    expect(uploadData).toHaveBeenCalledWith(expectedParameter);
  });

  it('should call datasources api to upload provided file and schema', () => {
    const data = { file: 'file', schema: 'schema' };

    const expectedParameter = {
      data: expect.any(FormData),
      headers: { 'content-type': 'multipart/form-data' },
      url: '/api/dataSources',
    };

    api.uploadFileAndSchema(data);

    expect(uploadData).toHaveBeenCalledWith(expectedParameter);
  });

  it('should call datasources api to get all datasources', () => {
    const expectedParameter = {
      url: '/api/dataSources',
      query: {
        dashboardId: 'dashboardId',
      },
    };

    api.getDatasources('dashboardId');

    expect(fetchData).toHaveBeenCalledWith(expectedParameter);
  });

  it('should call datasources/headers api to get headers for given datasource', () => {
    const dataId = 'id';
    const expectedParameter = {
      url: '/api/dataSources/id/headers',
    };

    api.getCsvHeaders(dataId);

    expect(fetchData).toHaveBeenCalledWith(expectedParameter);
  });

  it('should call datasources/data api to get data for given datasource', () => {
    const expectedParameter = {
      query: { columns: 'columns' },
      url: '/api/dataSources/id',
    };

    api.getData('id', 'columns');

    expect(fetchData).toHaveBeenCalledWith(expectedParameter);
  });

  it('should call projects api to get all the saved projects', () => {
    const expectedParameter = {
      url: '/api/projects',
    };

    api.getProjects();

    expect(fetchData).toHaveBeenCalledWith(expectedParameter);
  });

  it('should should call save new project with given name', () => {
    const projectData = JSON.stringify({ projectData: { name: 'untitled project' } });
    const expectedParameter = {
      data: projectData,
      headers: { 'content-type': 'application/json' },
      url: '/api/projects',
      method: 'post',
    };

    api.saveProject({ id: undefined, name: 'untitled project' });

    expect(uploadData).toHaveBeenCalledWith(expectedParameter);
  });

  it('should should update old project for given id with given data', () => {
    const projectData = JSON.stringify({
      projectData: { name: 'updated project', id: 'projectId' },
    });
    const expectedParameter = {
      data: projectData,
      headers: { 'content-type': 'application/json' },
      url: '/api/projects',
      method: 'put',
    };

    api.saveProject({ name: 'updated project', id: 'projectId' });

    expect(uploadData).toHaveBeenCalledWith(expectedParameter);
  });
  it('should call projects api with given project id', () => {
    const expectedParameter = {
      url: '/api/projects/1',
    };

    api.getProject('1');

    expect(fetchData).toHaveBeenCalledWith(expectedParameter);
  });
});
