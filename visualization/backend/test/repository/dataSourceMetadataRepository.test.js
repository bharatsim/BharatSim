const DataSourceMetaData = require('../../src/model/dataSourceMetadata');
const DataSourceMetaDataRepository = require('../../src/repository/dataSourceMetadataRepository');
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
      hour: 'number',
      susceptible: 'number',
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
});
