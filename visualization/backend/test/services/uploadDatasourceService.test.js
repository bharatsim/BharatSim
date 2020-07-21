const mongoose = require('mongoose');
const fs = require('fs');

const dataSourceMetadataRepository = require('../../src/repository/datasourceMetadataRepository');
const dataSourceRepository = require('../../src/repository/datasourceRepository');
const uploadDatasourceService = require('../../src/services/uploadDatasourceService');
const createModel = require('../../src/utils/modelCreator');

jest.mock('fs');
jest.mock('../../src/repository/datasourceMetadataRepository');
jest.mock('../../src/repository/datasourceRepository');
jest.mock('../../src/utils/modelCreator');
jest.mock('../../src/utils/csvParser', () => ({
  parseCSV: jest.fn().mockReturnValue({
    data: [
      { hour: 0, susceptible: 1 },
      { hour: 1, susceptible: 2 },
      { hour: 2, susceptible: 3 },
    ],
  }),
}));

describe('upload datasource  service', function () {
  describe('Uploaddatasource', function () {
    it('should insert schema and datasource name in dataSource metadata for uploaded csv', async function () {
      dataSourceMetadataRepository.insert.mockResolvedValue({ _id: 'collection' });

      const collectionId = await uploadDatasourceService.uploadCsv({
        path: '/uploads/1223',
        originalname: 'test.csv',
        mimetype: 'text/csv',
      });

      expect(collectionId).toEqual({ collectionId: 'collection' });
    });

    it('should insert schema and datasource name in dataSource metadata for uploaded csv', async function () {
      dataSourceMetadataRepository.insert.mockResolvedValue({ _id: new mongoose.Types.ObjectId(123123) });

      await uploadDatasourceService.uploadCsv({
        path: '/uploads/1223',
        originalname: 'test.csv',
        mimetype: 'text/csv',
      });

      expect(dataSourceMetadataRepository.insert).toHaveBeenCalledWith({
        dataSourceSchema: { hour: 'number', susceptible: 'number' },
        name: 'test.csv',
      });
    });

    it('should insert data in data source collection', async function () {
      dataSourceMetadataRepository.insert.mockResolvedValue({ _id: 'collectionId' });
      createModel.createModel.mockImplementation((id) => id);

      await uploadDatasourceService.uploadCsv({
        path: '/uploads/1223',
        originalname: 'test.csv',
        mimetype: 'text/csv',
      });

      expect(dataSourceRepository.insert).toHaveBeenCalledWith('collectionId', [
        { hour: 0, susceptible: 1 },
        { hour: 1, susceptible: 2 },
        { hour: 2, susceptible: 3 },
      ]);
    });
  });

  describe('deleteUploadedFile', function () {
    afterEach(() => {
      jest.clearAllMocks();
    });
    it('should delete file for given path', function () {
      fs.existsSync.mockReturnValue(true);

      uploadDatasourceService.deleteUploadedFile('path');

      expect(fs.rmdirSync).toHaveBeenCalledWith('path', { recursive: true });
    });

    it('should not delete file if given path not exist', function () {
      fs.existsSync.mockReturnValue(false);

      uploadDatasourceService.deleteUploadedFile('path');

      expect(fs.rmdirSync).not.toHaveBeenCalled();
    });
  });
});
