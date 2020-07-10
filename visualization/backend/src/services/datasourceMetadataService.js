const dataSourceMetadataRepository = require('../repository/datasourceMetadataRepository');

async function getHeaders(dataSourceName) {
  const dataSource = await dataSourceMetadataRepository.getDataSourceSchema(dataSourceName);
  const headers = Object.keys(dataSource.dataSourceSchema);
  return { headers };
}

async function getDataSources() {
  const dataSources = await dataSourceMetadataRepository.getDataSourceNames();
  const dataSourceNames = dataSources.map((dataSource) => dataSource.name);
  return { dataSources: dataSourceNames };
}

module.exports = {
  getHeaders,
  getDataSources,
};
