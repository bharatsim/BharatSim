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
  let insertedMetadata;
  let dataSourceId;

  beforeAll(async () => {
    await dbHandler.connect();
    insertedMetadata = await DataSourceMetaData.insertMany(dataSourceMetadata);
    const { _id } = insertedMetadata[0];
    dataSourceId = _id;
    await model1Model(dataSourceId.toString()).insertMany(model1);
  });
  afterAll(async () => {
    await dbHandler.clearDatabase();
    await dbHandler.closeDatabase();
  });

  describe('/datasources', () => {
    it('should get data source names', async () => {
      const expectedDataSource = insertedMetadata.map((metadata) => ({ _id: metadata.id, name: metadata.name }));
      await request(app).get('/datasources').expect(200).expect({ dataSources: expectedDataSource });
    });
  });

  describe('/datasources/:id/headers', () => {
    it('should get headers', async () => {
      await request(app)
        .get(`/datasources/${dataSourceId}/headers`)
        .expect(200)
        .expect({ headers: ['hour', 'susceptible'] });
    });

    it('should throw error if datasource not found', async () => {
      await request(app)
        .get(`/datasources/123456789012/headers`)
        .expect(404)
        .expect({ errorMessage: 'datasource with id 123456789012 not found' });
    });
  });

  describe('/datasources/:name/data', () => {
    it('should get data for requested columns', async () => {
      await request(app)
        .get(`/datasources/${dataSourceId}/data`)
        .query({ columns: ['susceptible', 'hour'] })
        .expect(200)
        .expect({ data: { susceptible: [1, 2, 3, 4, 5], hour: [0, 1, 2, 3, 4] } });
    });

    it('should throw error if data source not found', async () => {
      await request(app)
        .get('/datasources/123456789012/data')
        .query({ columns: ['expose', 'hour'] })
        .expect(404)
        .expect({ errorMessage: 'datasource with id 123456789012 not found' });
    });

    it('should send error message for columns not found exception', async () => {
      await request(app)
        .get(`/datasources/${dataSourceId}/data`)
        .query({ columns: ['exposeed', 'hour'] })
        .expect(200)
        .expect({});
    });
  });
});
