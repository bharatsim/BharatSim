const express = require('express');
const request = require('supertest');

const dataSourceMetadataService = require('../../src/services/dataSourceMetadataService');
const apiRoute = require('../../src/controller/api');

jest.mock('../../src/services/dataSourceMetadataService');
describe('api', () => {
  const app = express();
  app.use(apiRoute);
  beforeEach(() => {
    dataSourceMetadataService.getData.mockReturnValue({ columns: { exposed: [2, 3], hour: [1, 2] } });
    dataSourceMetadataService.getHeaders.mockReturnValue({ headers: ['hour', 'susceptible'] });
    dataSourceMetadataService.getDataSources.mockReturnValue({ dataSources: ['model_1', 'model_2'] });
  });

  it('should get data', async () => {
    await request(app)
      .get('/data')
      .expect(200)
      .expect({ columns: { exposed: [2, 3], hour: [1, 2] } });
    expect(dataSourceMetadataService.getData).toHaveBeenCalled();
  });

  it('should get data for requested columns', async () => {
    await request(app)
      .get('/data')
      .query({ columns: ['expose', 'hour'] })
      .expect(200)
      .expect({ columns: { exposed: [2, 3], hour: [1, 2] } });
    expect(dataSourceMetadataService.getData).toHaveBeenCalledWith(['expose', 'hour']);
  });

  it('should get headers', async () => {
    await request(app)
      .post('/headers')
      .expect(200)
      .expect({ headers: ['hour', 'susceptible'] });
    expect(dataSourceMetadataService.getHeaders).toHaveBeenCalled();
  });

  it('should get data source', async () => {
    await request(app)
      .get('/dataSources')
      .expect(200)
      .expect({ dataSources: ['model_1', 'model_2'] });
    expect(dataSourceMetadataService.getHeaders).toHaveBeenCalled();
  });
});
