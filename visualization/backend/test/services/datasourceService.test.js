const dataSourceService = require('../../src/services/datasourceService');
const dataSourceRepository = require('../../src/repository/datasourceRepository');
const dataSourceMetadataRepository = require('../../src/repository/datasourceMetadataRepository');
const modelCreator = require('../../src/utils/modelCreator');
const ColumnsNotFoundException = require('../../src/exceptions/ColumnsNotFoundException');

jest.mock('../../src/repository/dataSourceRepository');
jest.mock('../../src/repository/dataSourceMetadataRepository');
jest.mock('../../src/utils/modelCreator');

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
    dataSourceMetadataRepository.getDataSourceSchema.mockResolvedValue({ dataSourceSchema: 'DataSourceSchema' });
    dataSourceRepository.getData.mockResolvedValue([{ hour: 1 }, { hour: 2 }, { hour: 3 }]);
    modelCreator.createModel.mockReturnValue('DataSourceModel');
    const dataSourceName = 'model';

    const data = await dataSourceService.getData(dataSourceName, ['hour']);

    expect(dataSourceMetadataRepository.getDataSourceSchema).toHaveBeenCalledWith('model');
    expect(dataSourceRepository.getData).toHaveBeenCalledWith('DataSourceModel', { hour: 1 });
    expect(modelCreator.createModel).toHaveBeenCalledWith('model', 'DataSourceSchema');
    expect(data).toEqual({
      data: { hour: [1, 2, 3] },
    });
  });

  it('should throw an exception for column mismatch', async () => {
    dataSourceMetadataRepository.getDataSourceSchema.mockResolvedValue({ dataSourceSchema: 'DataSourceSchema' });
    modelCreator.createModel.mockReturnValue('DataSourceModel');
    dataSourceRepository.getData.mockResolvedValue([
      { hour: 1, susceptible: 99, exposed: 90 },
      { hour: 2, susceptible: 98, exposed: 90 },
      { hour: 3, susceptible: 97, exposed: 90 },
    ]);
    const dataSourceName = 'model';

    const result = async () => {
      await dataSourceService.getData(dataSourceName, ['hours', 'exposed']);
    };

    await expect(result).rejects.toThrow(ColumnsNotFoundException);
  });

  describe('mocked function testing', function () {
    beforeEach(() => {
      dataSourceMetadataRepository.getDataSourceSchema.mockResolvedValue({ dataSourceSchema: 'DataSourceSchema' });
      dataSourceRepository.getData.mockResolvedValue([{ hour: 1 }, { hour: 2 }, { hour: 3 }]);
      modelCreator.createModel.mockReturnValue('DataSourceModel');
      const dataSourceName = 'model';
      dataSourceService.getData(dataSourceName, ['hour']);
    });

    it('should getDataSourceSchema to have been called with dataSource name', function () {
      expect(dataSourceMetadataRepository.getDataSourceSchema).toHaveBeenCalledWith('model');
    });

    it('should dataSourceRepository.getData to have been called with created model', function () {
      expect(dataSourceRepository.getData).toHaveBeenCalledWith('DataSourceModel', { hour: 1 });
    });

    it('should createModel to have been called with dataSource name and schema', function () {
      expect(modelCreator.createModel).toHaveBeenCalledWith('model', 'DataSourceSchema');
    });
  });
});
