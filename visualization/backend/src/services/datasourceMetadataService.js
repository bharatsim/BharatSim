const dataSourceMetadataRepository = require('../repository/datasourceMetadataRepository');

function transformDataSourceSchema(dataSourceSchema) {
  return Object.keys(dataSourceSchema).map((key) => ({ name: key, type: dataSourceSchema[key] }));
}

async function getHeaders(dataSourceId) {
  const dataSource = await dataSourceMetadataRepository.getDataSourceSchemaById(dataSourceId);
  const headers = transformDataSourceSchema(dataSource.dataSourceSchema);
  return { headers };
}

async function getDataSources() {
  const dataSources = await dataSourceMetadataRepository.getDataSourceNames();
  return { dataSources };
}

module.exports = {
  getHeaders,
  getDataSources,
};
