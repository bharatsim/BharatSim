const mongoose = require('mongoose');

const DataSourceMetaData = require('../../src/model/datasourceMetadata');
const DataSourceMetaDataRepository = require('../../src/repository/datasourceMetadataRepository');
const DataSourceNotFoundExceeption = require('../../src/exceptions/DatasourceNotFoundException');
const dbHandler = require('../db-handler');

const dataSourceMetadata = [
  {
    name: 'model_1',
    dataSourceSchema: {
      hour: 'number',
      susceptible: 'number',
    },
  },
  {
    name: 'model_2',
    dataSourceSchema: {
      hour_: 'number',
      susceptible_: 'number',
    },
  },
];

const parseMongoDBResult = (result) => JSON.parse(JSON.stringify(result));

describe('get Datasource name ', () => {
  beforeAll(async () => {
    await dbHandler.connect();
  });
  afterEach(async () => {
    await dbHandler.clearDatabase();
  });
  afterAll(async () => {
    await dbHandler.closeDatabase();
  });

  describe('getDataSourceNames', function () {
    it('should return names of all present data sources', async () => {
      const insertedMetadata = await DataSourceMetaData.insertMany(dataSourceMetadata);
      const expectedResult = insertedMetadata.map((metadata) => ({ _id: metadata.id, name: metadata.name }));

      const names = parseMongoDBResult(await DataSourceMetaDataRepository.getDataSourceNames());

      expect(names).toEqual(expectedResult);
    });
  });

  describe('getDataSourceSchemaById', function () {
    it('should return datasource schema for given datasource name', async () => {
      const insertedMetadata = await DataSourceMetaData.insertMany(dataSourceMetadata);
      const { _id: dataSourceId } = insertedMetadata[0];
      const schema = parseMongoDBResult(await DataSourceMetaDataRepository.getDataSourceSchemaById(dataSourceId));

      expect(schema).toEqual({
        dataSourceSchema: {
          hour: 'number',
          susceptible: 'number',
        },
      });
    });

    it('should throw exception if datasource is not available', async () => {
      await DataSourceMetaData.insertMany(dataSourceMetadata);
      const dataSourceId = new mongoose.Types.ObjectId('123112123112');

      await expect(DataSourceMetaDataRepository.getDataSourceSchemaById(dataSourceId)).rejects.toBeInstanceOf(
        DataSourceNotFoundExceeption,
      );
      await expect(DataSourceMetaDataRepository.getDataSourceSchemaById(dataSourceId)).rejects.toEqual(
        new DataSourceNotFoundExceeption(dataSourceId),
      );
    });
  });

  it('should insert a data for dataSource Metadata', async () => {
    await DataSourceMetaDataRepository.insert({
      name: 'model_1',
      dataSourceSchema: {
        hour: 'number',
        susceptible: 'number',
      },
    });

    const result = parseMongoDBResult(await DataSourceMetaData.findOne({ name: 'model_1' }, { _id: 0, __v: 0 }));

    expect(result).toEqual({
      name: 'model_1',
      dataSourceSchema: {
        hour: 'number',
        susceptible: 'number',
      },
    });
  });
});
