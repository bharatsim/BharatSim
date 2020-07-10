const dataSourceService = require('../../src/services/datasourceService');
const dataSourceRepository = require('../../src/repository/datasourceRepository');
const dataSourceMetadataRepository = require('../../src/repository/datasourceMetadataRepository');
const modelCreator = require('../../src/model/modelCreator');

jest.mock('../../src/repository/dataSourceRepository');
jest.mock('../../src/repository/dataSourceMetadataRepository');
jest.mock('../../src/model/modelCreator');
jest.mock('fs');

describe('dataSourceService', () => {
  it('should fetch data from database for give datasource name', async () => {
    dataSourceMetadataRepository.getDataSourceSchema.mockResolvedValue('DataSourceSchema');
    modelCreator.createModel.mockReturnValue('DataSourceModel');
    dataSourceRepository.getData.mockResolvedValue([
      { hour: 1, susceptible: 99 },
      { hour: 2, susceptible: 98 },
      { hour: 3, susceptible: 97 },
    ]);

    const dataSourceName = 'model';

    const data = await dataSourceService.getData(dataSourceName);

    expect(data).toEqual({
      data: {
        hour: [1, 2, 3],
        susceptible: [99, 98, 97],
      },
    });
  });

  it('should fetch data from database for give datasource name and selected columns only', async () => {
    dataSourceMetadataRepository.getDataSourceSchema.mockResolvedValue('DataSourceSchema');
    dataSourceRepository.getData.mockResolvedValue([{ hour: 1 }, { hour: 2 }, { hour: 3 }]);
    modelCreator.createModel.mockReturnValue('DataSourceModel');
    const dataSourceName = 'model';

    const data = await dataSourceService.getData(dataSourceName, ['hour']);

    expect(dataSourceMetadataRepository.getDataSourceSchema).toHaveBeenCalledWith('model');
    expect(dataSourceRepository.getData).toHaveBeenCalledWith('DataSourceModel', { hour: 1 });
    expect(modelCreator.createModel).toHaveBeenCalledWith('DataSourceSchema');
    expect(data).toEqual({
      data: { hour: [1, 2, 3] },
    });
  });
});
