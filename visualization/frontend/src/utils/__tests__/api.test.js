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
      data:
        '{"dashboardData":{"widgets":[],"layout":[],"dashboardId":"id","name":"name","count":0}}',
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
    };

    api.getDatasources();

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

  it('should call datasources/headers api to get headers for given datasource', () => {
    const dataId = 'id';
    const expectedParameter = {
      url: '/api/dataSources/id/headers',
    };

    api.getCsvHeaders(dataId);

    expect(fetchData).toHaveBeenCalledWith(expectedParameter);
  });

  it('should call datasources/data api to get data for given datasource', () => {
    const options = {
      params: 'id',
      query: 'query',
    };
    const expectedParameter = {
      query: 'query',
      url: '/api/dataSources/id',
    };

    api.getData(options);

    expect(fetchData).toHaveBeenCalledWith(expectedParameter);
  });
});
