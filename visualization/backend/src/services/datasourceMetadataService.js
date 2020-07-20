const dataSourceMetadataRepository = require('../repository/datasourceMetadataRepository');

async function getHeaders(dataSourceId) {
  const dataSource = await dataSourceMetadataRepository.getDataSourceSchemaById(dataSourceId);
  const headers = Object.keys(dataSource.dataSourceSchema);
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
