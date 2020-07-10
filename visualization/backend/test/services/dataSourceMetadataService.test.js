const dataSourceMetadataService = require('../../src/services/datasourceMetadataService');
const dataSourceMetadataRepository = require('../../src/repository/datasourceMetadataRepository');

jest.mock('../../src/repository/dataSourceMetadataRepository');

describe('dataSourceMetadataService', () => {
  it('should get data sources name', async () => {
    const mockResolvedValue = [{ name: 'model_1' }, { name: 'model_2' }];
    dataSourceMetadataRepository.getDataSourceNames.mockResolvedValue(mockResolvedValue);

    const data = await dataSourceMetadataService.getDataSources();

    expect(data).toEqual({
      dataSources: ['model_1', 'model_2'],
    });
  });

  it('should get headers from datasource', async () => {
    dataSourceMetadataRepository.getDataSourceSchema.mockResolvedValue({
      dataSourceSchema: {
        hour: 'number',
        susceptible: 'number',
      },
    });
    const dataSourceName = 'model_1';

    const data = await dataSourceMetadataService.getHeaders(dataSourceName);

    expect(data).toEqual({
      headers: ['hour', 'susceptible'],
    });
  });
});
