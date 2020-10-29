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

async function insert({ name, dataSourceSchema, dashboardId, fileType, fileSize }) {
  const dataSourceMetadata = new DataSourceMetadata({
    name,
    dataSourceSchema,
    dashboardId,
    fileSize,
    fileType,
  });
  return dataSourceMetadata.save();
}

async function deleteDatasourceMetadata(dataSourceId) {
  DataSourceMetadata.deleteOne({ _id: dataSourceId }).exec();
}

async function getDataSourcesMetadataByDashboardId(dashboardId) {
  return DataSourceMetadata.find({ dashboardId }, { __v: 0, dataSourceSchema: 0 }).then(
    (data) => data,
  );
}

module.exports = {
  getDataSourceNames,
  getDataSourceSchemaById,
  insert,
  deleteDatasourceMetadata,
  getDataSourcesMetadataByDashboardId,
};
