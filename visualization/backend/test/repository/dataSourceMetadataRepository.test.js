const DataSourceMetaData = require('../../src/model/dataSourceMetadata');
const DataSourceMetaDataRepository = require('../../src/repository/dataSourceMetadataRepository');
const DataSourceNotFoundExceeption = require('../../src/exceptions/DataSourceNotFoundException');
const dbHandler = require('./db-handler');

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

  it('should return names of all present data sources', async () => {
    await DataSourceMetaData.insertMany(dataSourceMetadata);

    const names = parseMongoDBResult(await DataSourceMetaDataRepository.getDataSourceNames());

    expect(names).toEqual([{ name: 'model_1' }, { name: 'model_2' }]);
  });

  it('should return datasource schema for given datasource name', async () => {
    await DataSourceMetaData.insertMany(dataSourceMetadata);
    const dataSourceName = 'model_1';
    const names = parseMongoDBResult(await DataSourceMetaDataRepository.getDataSourceSchema(dataSourceName));

    expect(names).toEqual({
      dataSourceSchema: {
        hour: 'number',
        susceptible: 'number',
      },
    });
  });

  it('should throw exception if datasource is not available', async () => {
    await DataSourceMetaData.insertMany(dataSourceMetadata);
    const dataSourceName = 'model_3';

    await expect(DataSourceMetaDataRepository.getDataSourceSchema(dataSourceName)).rejects.toBeInstanceOf(
      DataSourceNotFoundExceeption,
    );
    await expect(DataSourceMetaDataRepository.getDataSourceSchema(dataSourceName)).rejects.toEqual(
      new DataSourceNotFoundExceeption(dataSourceName),
    );
  });
});
