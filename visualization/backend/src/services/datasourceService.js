const dataSourceRepository = require('../repository/datasourceRepository');
const dataSourceMetadataRepository = require('../repository/datasourceMetadataRepository');
const modelCreator = require('../utils/modelCreator');
const dbUtils = require('../utils/dbUtils');
const ColumnsNotFoundException = require('../exceptions/ColumnsNotFoundException');

function isNotProvidedDataHaveEqualColumns(data, columns) {
  return columns.length !== Object.keys(data).length;
}

async function getDataSourceModel(dataSourceName) {
  const dataSourceSchema = await dataSourceMetadataRepository.getDataSourceSchema(dataSourceName);
  return modelCreator.createModel(dataSourceName, dataSourceSchema.dataSourceSchema);
}

async function getData(dataSourceName, columns) {
  const dataSourceModel = await getDataSourceModel(dataSourceName);
  const columnsMap = dbUtils.getProjectedColumns(columns);
  const dataRecords = await dataSourceRepository.getData(dataSourceModel, columnsMap);
  const data = dbUtils.changeRecordDimensionToArray(dataRecords);
  if (columns && isNotProvidedDataHaveEqualColumns(data, columns)) {
    throw new ColumnsNotFoundException();
  }
  return { data };
}

module.exports = {
  getData,
};
