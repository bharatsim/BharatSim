const DataSourceMetadata = require('../model/datasourceMetadata');
const DataSourceNotFoundException = require('../exceptions/DatasourceNotFoundException');

async function getDataSourceNames() {
  return DataSourceMetadata.find()
    .select('name')
    .then((data) => data);
}

async function getDataSourceSchemaById(dataSourceId) {
  return DataSourceMetadata.findOne({ _id: dataSourceId }, { _id: 0 })
    .select('dataSourceSchema')
    .then((data) => {
      if (!data) {
        throw new DataSourceNotFoundException(dataSourceId);
      }
      return data;
    });
}

async function insert({ name, dataSourceSchema, dashboardId }) {
  const dataSourceMetadata = new DataSourceMetadata({ name, dataSourceSchema, dashboardId });
  return dataSourceMetadata.save();
}

async function deleteDatasourceMetadata(dataSourceId) {
  DataSourceMetadata.deleteOne({ _id: dataSourceId }).exec();
}

module.exports = {
  getDataSourceNames,
  getDataSourceSchemaById,
  insert,
  deleteDatasourceMetadata,
};
