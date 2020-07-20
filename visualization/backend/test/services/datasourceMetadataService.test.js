const dataSourceMetadataService = require('../../src/services/datasourceMetadataService');
const dataSourceMetadataRepository = require('../../src/repository/datasourceMetadataRepository');

jest.mock('../../src/repository/dataSourceMetadataRepository');

describe('dataSourceMetadataService', () => {
  it('should get data sources name', async () => {
    const mockResolvedValue = [
      { _id: 'id1', name: 'model_1' },
      { _id: 'id2', name: 'model_2' },
    ];
    dataSourceMetadataRepository.getDataSourceNames.mockResolvedValue(mockResolvedValue);

    const data = await dataSourceMetadataService.getDataSources();

    expect(data).toEqual({
      dataSources: [
        { _id: 'id1', name: 'model_1' },
        { _id: 'id2', name: 'model_2' },
      ],
    });
  });

  it('should get headers from datasource', async () => {
    dataSourceMetadataRepository.getDataSourceSchemaById.mockResolvedValue({
      dataSourceSchema: {
        hour: 'number',
        susceptible: 'number',
      },
    });
    const dataSourceId = 'model_1_id';

    const data = await dataSourceMetadataService.getHeaders(dataSourceId);

    expect(data).toEqual({
      headers: ['hour', 'susceptible'],
    });
  });
});
