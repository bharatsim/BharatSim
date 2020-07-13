const express = require('express');
const request = require('supertest');

const dataSourceMetadataService = require('../../src/services/datasourceMetadataService');
const datasourceService = require('../../src/services/datasourceService');
const apiRoute = require('../../src/controller/api');
const DataSourceNotFoundException = require('../../src/exceptions/DatasourceNotFoundException');
const ColumnsNotFoundException = require('../../src/exceptions/ColumnsNotFoundException');

jest.mock('../../src/services/dataSourceMetadataService');
jest.mock('../../src/services/dataSourceService');

describe('api', () => {
  const app = express();
  app.use(express.json());
  app.use(express.urlencoded({ extended: true }));
  app.use(apiRoute);

  beforeEach(() => {
    datasourceService.getData.mockResolvedValue({ data: { exposed: [2, 3], hour: [1, 2] } });
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

    it('should send error for any technical error', async () => {
      dataSourceMetadataService.getHeaders.mockRejectedValueOnce(new Error('error'));

      await request(app)
        .get('/datasources/datasourceName/headers')
        .expect(500)
        .expect({ errorMessage: 'Technical error error' });

      expect(dataSourceMetadataService.getHeaders).toHaveBeenCalledWith('datasourceName');
    });
  });

  describe('/datasources/:name/data', () => {
    it('should get data for specified datasource name', async () => {
      await request(app)
        .get('/datasources/datasourceName/data')
        .expect(200)
        .expect({ data: { exposed: [2, 3], hour: [1, 2] } });
      expect(datasourceService.getData).toHaveBeenCalledWith('datasourceName', undefined);
    });

    it('should get data for requested columns', async () => {
      await request(app)
        .get('/datasources/datasourceName/data')
        .query({ columns: ['expose', 'hour'] })
        .expect(200)
        .expect({ data: { exposed: [2, 3], hour: [1, 2] } });
      expect(datasourceService.getData).toHaveBeenCalledWith('datasourceName', ['expose', 'hour']);
    });

    it('should throw error if data source not found', async () => {
      const dataSourceNotFoundException = new DataSourceNotFoundException('datasourceName');
      datasourceService.getData.mockRejectedValueOnce(dataSourceNotFoundException);

      await request(app)
        .get('/datasources/datasourceName/data')
        .query({ columns: ['expose', 'hour'] })
        .expect(404)
        .expect({ errorMessage: 'datasource with name datasourceName not found' });

      expect(datasourceService.getData).toHaveBeenCalledWith('datasourceName', ['expose', 'hour']);
    });

    it('should send error message for columns not found exception', async () => {
      const columnsNotFoundException = new ColumnsNotFoundException();
      datasourceService.getData.mockRejectedValueOnce(columnsNotFoundException);

      await request(app)
        .get('/datasources/datasourceName/data')
        .query({ columns: ['exposeed', 'hour'] })
        .expect(200)
        .expect({ errorMessage: 'One or more columns not found' });

      expect(datasourceService.getData).toHaveBeenCalledWith('datasourceName', ['exposeed', 'hour']);
    });

    it('should throw error if any technical error occur', async () => {
      datasourceService.getData.mockRejectedValueOnce(new Error('error'));

      await request(app)
        .get('/datasources/datasourceName/data')
        .query({ columns: ['expose', 'hour'] })
        .expect(500)
        .expect({ errorMessage: 'Technical error error' });

      expect(datasourceService.getData).toHaveBeenCalledWith('datasourceName', ['expose', 'hour']);
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

    it('should send error message for columns not found exception', async () => {
      dataSourceMetadataService.getDataSources.mockRejectedValueOnce(new Error('error'));

      await request(app)
        .get('/datasources')
        .query({ columns: ['exposeed', 'hour'] })
        .expect(500)
        .expect({ errorMessage: 'Technical error error' });

      expect(dataSourceMetadataService.getDataSources).toHaveBeenCalled();
    });
  });
});
