const express = require('express');
const request = require('supertest');
const multer = require('multer');
const fs = require('fs');

const dataSourceMetadataService = require('../../src/services/datasourceMetadataService');
const datasourceService = require('../../src/services/datasourceService');
const uploadDatasourceService = require('../../src/services/uploadDatasourceService');
const apiRoute = require('../../src/controller/api');
const DataSourceNotFoundException = require('../../src/exceptions/DatasourceNotFoundException');
const ColumnsNotFoundException = require('../../src/exceptions/ColumnsNotFoundException');
const InvalidInputException = require('../../src/exceptions/InvalidInputException');

const TEST_FILE_UPLOAD_PATH = './test/testUpload/';
jest.mock('multer');
jest.mock('../../src/services/dataSourceMetadataService');
jest.mock('../../src/services/dataSourceService');
jest.mock('../../src/services/uploadDatasourceService');

multer.mockImplementation(() => ({
  single() {
    return (req, res, next) => {
      req.body = { title: req.query.title };
      req.file = { originalname: 'sample.name', mimetype: 'sample.type', path: 'sample.path' };
      return next();
    };
  },
}));

describe('api', () => {
  const app = express();
  app.use(express.json());
  app.use(express.urlencoded({ extended: true }));
  app.use(multer({ dest: TEST_FILE_UPLOAD_PATH }).single('datafile'));
  app.use(apiRoute);

  beforeEach(() => {
    datasourceService.getData.mockResolvedValue({ data: { exposed: [2, 3], hour: [1, 2] } });
    dataSourceMetadataService.getHeaders.mockResolvedValue({ headers: ['hour', 'susceptible'] });
    dataSourceMetadataService.getDataSources.mockResolvedValue({
      dataSources: [{ name: 'model_1' }, { name: 'model_2' }],
    });
    uploadDatasourceService.uploadCsv.mockResolvedValue({ collectionId: 'id' });
  });

  afterAll(() => {
    if (fs.existsSync(TEST_FILE_UPLOAD_PATH)) {
      fs.rmdirSync(TEST_FILE_UPLOAD_PATH, { recursive: true });
    }
  });

  describe('Get /datasources/:id/headers', () => {
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
        .expect({ errorMessage: 'datasource with id model_1 not found' });
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

  describe('Get /datasources/:id', () => {
    it('should get data for specified datasource name', async () => {
      await request(app)
        .get('/datasources/datasourceId')
        .expect(200)
        .expect({ data: { exposed: [2, 3], hour: [1, 2] } });
      expect(datasourceService.getData).toHaveBeenCalledWith('datasourceId', undefined);
    });

    it('should get data for requested columns', async () => {
      await request(app)
        .get('/datasources/datasourceId')
        .query({ columns: ['expose', 'hour'] })
        .expect(200)
        .expect({ data: { exposed: [2, 3], hour: [1, 2] } });
      expect(datasourceService.getData).toHaveBeenCalledWith('datasourceId', ['expose', 'hour']);
    });

    it('should throw error if data source not found', async () => {
      const dataSourceNotFoundException = new DataSourceNotFoundException('datasourceName');
      datasourceService.getData.mockRejectedValueOnce(dataSourceNotFoundException);

      await request(app)
        .get('/datasources/datasourceId')
        .query({ columns: ['expose', 'hour'] })
        .expect(404)
        .expect({ errorMessage: 'datasource with id datasourceName not found' });

      expect(datasourceService.getData).toHaveBeenCalledWith('datasourceId', ['expose', 'hour']);
    });

    it('should send error message for columns not found exception', async () => {
      const columnsNotFoundException = new ColumnsNotFoundException();
      datasourceService.getData.mockRejectedValueOnce(columnsNotFoundException);

      await request(app)
        .get('/datasources/datasourceId')
        .query({ columns: ['exposeed', 'hour'] })
        .expect(200)
        .expect({});

      expect(datasourceService.getData).toHaveBeenCalledWith('datasourceId', ['exposeed', 'hour']);
    });

    it('should throw error if any technical error occur', async () => {
      datasourceService.getData.mockRejectedValueOnce(new Error('error'));

      await request(app)
        .get('/datasources/datasourceId')
        .query({ columns: ['expose', 'hour'] })
        .expect(500)
        .expect({ errorMessage: 'Technical error error' });

      expect(datasourceService.getData).toHaveBeenCalledWith('datasourceId', ['expose', 'hour']);
    });
  });

  describe('Get /datasources', () => {
    it('should get data source names', async () => {
      await request(app)
        .get('/datasources')
        .expect(200)
        .expect({ dataSources: [{ name: 'model_1' }, { name: 'model_2' }] });
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

  describe('Post /datasources', function () {
    it('should upload file successfully', async () => {
      await request(app)
        .post('/datasources')
        .field('name', 'datafile')
        .attach('datafile', 'test/data/simulation.csv')
        .expect(200)
        .expect({ collectionId: 'id' });
    });

    it('should delete file after successful upload', async () => {
      await request(app)
        .post('/datasources')
        .field('name', 'datafile')
        .attach('datafile', 'test/data/simulation.csv')
        .expect(200)
        .expect({ collectionId: 'id' });

      expect(uploadDatasourceService.deleteUploadedFile).toHaveBeenCalledWith('sample.path');
    });

    it('should delete file after unsuccessful upload', async () => {
      uploadDatasourceService.uploadCsv.mockRejectedValueOnce(new Error());
      await request(app)
        .post('/datasources')
        .field('name', 'datafile')
        .attach('datafile', 'test/data/test.png')
        .expect(500)
        .expect({ errorMessage: 'Technical error ' });

      expect(uploadDatasourceService.deleteUploadedFile).toHaveBeenCalledWith('sample.path');
    });

    it('should throw an invalid input exception for file type not match', async () => {
      const invalidInputError = new InvalidInputException('error message');
      uploadDatasourceService.uploadCsv.mockRejectedValueOnce(invalidInputError);
      await request(app)
        .post('/datasources')
        .field('name', 'datafile')
        .attach('datafile', 'test/data/test.png')
        .expect(400)
        .expect({ errorMessage: 'error message' });

      expect(uploadDatasourceService.uploadCsv).toHaveBeenCalledWith({
        mimetype: 'sample.type',
        originalname: 'sample.name',
        path: 'sample.path',
      });
    });

    it('should throw an technical exception for file type not match', async () => {
      uploadDatasourceService.uploadCsv.mockRejectedValueOnce(new Error());
      await request(app)
        .post('/datasources')
        .field('name', 'datafile')
        .attach('datafile', 'test/data/test.png')
        .expect(500)
        .expect({ errorMessage: 'Technical error ' });
    });
  });
});
