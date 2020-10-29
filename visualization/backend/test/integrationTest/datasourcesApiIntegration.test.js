const express = require('express');
const request = require('supertest');
const multer = require('multer');
const mongoose = require('mongoose');

const dbHandler = require('../db-handler');
const DataSourceMetaData = require('../../src/model/datasourceMetadata');
const { dataSourceMetadata, model1, model1Model } = require('./data');
const datasourcesRoutes = require('../../src/controller/datasourcesController');
const { parseDBObject } = require('../../src/utils/dbUtils');

const TEST_FILE_UPLOAD_PATH = './uploads/';

describe('Integration test', () => {
  const app = express();
  app.use(express.json());
  app.use(express.urlencoded({ extended: true }));
  app.use(multer({ dest: TEST_FILE_UPLOAD_PATH }).single('datafile'));
  app.use('/datasources', datasourcesRoutes);
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
    it('should get data sources filter by dashboard id', async () => {
      const expectedDataSource = insertedMetadata.map((metadata) => ({
        _id: metadata.id,
        name: metadata.name,
        fileSize: metadata.fileSize,
        fileType: metadata.fileType,
        dashboardId: metadata.dashboardId.toString(),
        createdAt: metadata.createdAt.toISOString(),
        updatedAt: metadata.updatedAt.toISOString(),
      }));
      await request(app)
        .get('/datasources?dashboardId=313233343536373839303137')
        .expect(200)
        .expect({ dataSources: expectedDataSource });
    });
  });

  describe('/datasources/:id/headers', () => {
    it('should get headers', async () => {
      await request(app)
        .get(`/datasources/${dataSourceId}/headers`)
        .expect(200)
        .expect({
          headers: [
            { name: 'hour', type: 'number' },
            { name: 'susceptible', type: 'number' },
          ],
        });
    });

    it('should throw error if datasource not found', async () => {
      await request(app)
        .get(`/datasources/123456789012/headers`)
        .expect(404)
        .expect({ errorMessage: 'datasource with id 123456789012 not found' });
    });
  });

  describe('/datasources/:id/', () => {
    it('should get data for requested columns', async () => {
      await request(app)
        .get(`/datasources/${dataSourceId}`)
        .query({ columns: ['susceptible', 'hour'] })
        .expect(200)
        .expect({ data: { susceptible: [1, 2, 3, 4, 5], hour: [0, 1, 2, 3, 4] } });
    });

    it('should throw error if data source not found', async () => {
      await request(app)
        .get('/datasources/123456789012')
        .query({ columns: ['expose', 'hour'] })
        .expect(404)
        .expect({ errorMessage: 'datasource with id 123456789012 not found' });
    });

    it('should send error message for columns not found exception', async () => {
      await request(app)
        .get(`/datasources/${dataSourceId}`)
        .query({ columns: ['exposeed', 'hour'] })
        .expect(200)
        .expect({});
    });
  });

  describe('Post /datasources', function () {
    it('should uploaded file in database with 200 as http response', async function () {
      const testSchemaModal1 = {
        hour: 'Number',
        susceptible: 'Number',
        exposed: 'Number',
        infected: 'Number',
        hospitalized: 'Number',
        recovered: 'Number',
        deceased: 'Number',
        city: 'String',
      };

      const response = await request(app)
        .post('/datasources')
        .field('schema', JSON.stringify(testSchemaModal1))
        .field('dashboardId', '313233343536373839303137')
        .field('name', 'datafile')
        .attach('datafile', 'test/data/simulation.csv')
        .expect(200);

      const uploadedFileCollectionId = response.body.collectionId;
      const { _id, dataSourceSchema } = parseDBObject(
        await DataSourceMetaData.findOne({ _id: uploadedFileCollectionId }),
      );
      const collections = Object.keys(mongoose.connections[0].collections);

      expect(_id).toEqual(uploadedFileCollectionId);
      expect(dataSourceSchema).toEqual(testSchemaModal1);
      expect(collections.includes(uploadedFileCollectionId).toString()).toBe('true');
    });

    it('should provide a error when invalid file is uploaded', async function () {
      const testSchemaModal1 = {
        hour: 'Number',
        susceptible: 'Number',
        exposed: 'Number',
        infected: 'Number',
        hospitalized: 'Number',
        recovered: 'Number',
        deceased: 'Number',
        city: 'String',
      };

      await request(app)
        .post('/datasources')
        .field('schema', JSON.stringify(testSchemaModal1))
        .field('dashboardId', '313233343536373839303137')
        .field('name', 'datafile')
        .attach('datafile', 'test/data/test.png')
        .expect(400)
        .expect({ errorMessage: 'Invalid Input - File type does not match' });
    });
    it('should throw and error if column data and schema are not compatible', async function () {
      const testSchemaModal1 = {
        hour: 'Number',
        susceptible: 'Number',
        exposed: 'Number',
        infected: 'Number',
        hospitalized: 'Number',
        recovered: 'Number',
        deceased: 'Number',
        city: 'Number',
      };

      await request(app)
        .post('/datasources')
        .field('schema', JSON.stringify(testSchemaModal1))
        .field('dashboardId', '313233343536373839303137')
        .field('name', 'datafile')
        .attach('datafile', 'test/data/simulation.csv')
        .expect(400)
        .expect({ errorMessage: 'Invalid Input - Error while uploading csv file data' });
    });
  });
});
