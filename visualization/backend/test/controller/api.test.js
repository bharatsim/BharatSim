const express = require('express');
const request = require('supertest');

const dataSourceMetadataService = require('../../src/services/dataSourceMetadataService');
const apiRoute = require('../../src/controller/api');
const DataSourceNotFoundException = require('../../src/exceptions/DataSourceNotFoundException');

jest.mock('../../src/services/dataSourceMetadataService');

describe('api', () => {
  const app = express();
  app.use(express.json());
  app.use(express.urlencoded({ extended: true }));
  app.use(apiRoute);

  beforeEach(() => {
    dataSourceMetadataService.getData.mockReturnValue({ columns: { exposed: [2, 3], hour: [1, 2] } });
    dataSourceMetadataService.getHeaders.mockResolvedValue({ headers: ['hour', 'susceptible'] });
    dataSourceMetadataService.getDataSources.mockResolvedValue({ dataSources: ['model_1', 'model_2'] });
  });

  describe('/datasources/:name/headers', () => {
    it('should get headers', async () => {
      await request(app)
        .get('/datasources/model_1/headers')
        .expect(200)
        .expect({ headers: ['hour', 'susceptible'] });
      expect(dataSourceMetadataService.getHeaders).toHaveBeenCalledWith('model_1');
    });

    it('should throw error if datasource not found', async () => {
      const dataSourceNotFoundException = new DataSourceNotFoundException('model_1');
      dataSourceMetadataService.getHeaders.mockRejectedValueOnce(dataSourceNotFoundException);
      await request(app)
        .get('/datasources/model_1/headers')
        .expect(404)
        .expect({ errorMessage: 'datasource with name model_1 not found' });
      expect(dataSourceMetadataService.getHeaders).toHaveBeenCalledWith('model_1');
    });
  });

  describe('/data', () => {
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
  });

  describe('/datasources', () => {
    it('should get data source names', async () => {
      await request(app)
        .get('/datasources')
        .expect(200)
        .expect({ dataSources: ['model_1', 'model_2'] });
      expect(dataSourceMetadataService.getDataSources).toHaveBeenCalled();
    });
  });
});
