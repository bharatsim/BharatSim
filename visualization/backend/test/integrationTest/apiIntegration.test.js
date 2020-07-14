const express = require('express');
const request = require('supertest');

const dbHandler = require('../db-handler');
const DataSourceMetaData = require('../../src/model/datasourceMetadata');
const { dataSourceMetadata, model1, model1Model } = require('./data');
const apiRoute = require('../../src/controller/api');

describe('Integration test', () => {
  const app = express();
  app.use(express.json());
  app.use(express.urlencoded({ extended: true }));
  app.use(apiRoute);

  beforeAll(async () => {
    await dbHandler.connect();
    await DataSourceMetaData.insertMany(dataSourceMetadata);
    await model1Model.insertMany(model1);
  });
  afterAll(async () => {
    await dbHandler.clearDatabase();
    await dbHandler.closeDatabase();
  });

  describe('/datasources', () => {
    it('should get data source names', async () => {
      await request(app)
        .get('/datasources')
        .expect(200)
        .expect({ dataSources: ['model_1', 'model_2'] });
    });
  });

  describe('/datasources/:name/headers', () => {
    it('should get headers', async () => {
      await request(app)
        .get('/datasources/model_1/headers')
        .expect(200)
        .expect({ headers: ['hour', 'susceptible'] });
    });

    it('should throw error if datasource not found', async () => {
      await request(app)
        .get('/datasources/model_3/headers')
        .expect(404)
        .expect({ errorMessage: 'datasource with name model_3 not found' });
    });
  });

  describe('/datasources/:name/data', () => {
    it('should get data for requested columns', async () => {
      await request(app)
        .get('/datasources/model_1/data')
        .query({ columns: ['susceptible', 'hour'] })
        .expect(200)
        .expect({ data: { susceptible: [1, 2, 3, 4, 5], hour: [0, 1, 2, 3, 4] } });
    });

    it('should throw error if data source not found', async () => {
      await request(app)
        .get('/datasources/datasourceName/data')
        .query({ columns: ['expose', 'hour'] })
        .expect(404)
        .expect({ errorMessage: 'datasource with name datasourceName not found' });
    });

    it('should send error message for columns not found exception', async () => {
      await request(app)
        .get('/datasources/model_1/data')
        .query({ columns: ['exposeed', 'hour'] })
        .expect(200)
        .expect({});
    });
  });
});
