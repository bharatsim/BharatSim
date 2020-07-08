const fs = require('fs');

const dataSourceMetadataService = require('../../src/services/dataSourceMetadataService');
const dataSourceMetadataRepository = require('../../src/repository/dataSourceMetadataRepository');

jest.mock('../../src/repository/dataSourceMetadataRepository');
jest.mock('fs');

describe('csvService', () => {
  beforeEach(() => {
    fs.readFileSync.mockReturnValue(
      'hour,susceptible,exposed,infected,hospitalized,recovered,deceased\n1,9999,1,0,0,0,0\n2,9999,1,0,0,0,0',
    );
  });

  it('should get data for all the columns from csv', () => {
    const data = dataSourceMetadataService.getData();
    expect(data).toEqual({
      columns: {
        deceased: [0, 0],
        exposed: [1, 1],
        hospitalized: [0, 0],
        hour: [1, 2],
        infected: [0, 0],
        recovered: [0, 0],
        susceptible: [9999, 9999],
      },
    });
  });
  it('should get data only for selected columns from csv', () => {
    const data = dataSourceMetadataService.getData(['susceptible', 'hour']);
    expect(data).toEqual({ columns: { susceptible: [9999, 9999], hour: [1, 2] } });
  });
  it('should get headers from csv', () => {
    const data = dataSourceMetadataService.getHeaders();
    expect(data).toEqual({
      headers: ['hour', 'susceptible', 'exposed', 'infected', 'hospitalized', 'recovered', 'deceased'],
    });
  });

  it('should get data sources name', async () => {
    dataSourceMetadataRepository.getDataSourceNames.mockResolvedValue([{ name: 'model_1' }, { name: 'model_2' }]);
    const data = await dataSourceMetadataService.getDataSources();
    expect(data).toEqual({
      dataSources: ['model_1', 'model_2'],
    });
  });
});
