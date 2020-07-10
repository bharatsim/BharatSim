const dataSourceRepository = require('../repository/datasourceRepository');
const dataSourceMetadataRepository = require('../repository/datasourceMetadataRepository');
const modelCreator = require('../model/modelCreator');
const dbUtils = require('../utils/dbUtils');

async function getData(dataSourceName, columns) {
  const dataSourceSchema = await dataSourceMetadataRepository.getDataSourceSchema(dataSourceName);
  const dataSourceModel = modelCreator.createModel(dataSourceSchema);
  const columnsMap = dbUtils.getProjectedColumns(columns);
  const dataRecords = await dataSourceRepository.getData(dataSourceModel, columnsMap);
  const data = dbUtils.changeRecordDimensionToArray(dataRecords);
  return { data };
}

module.exports = {
  getData,
};
