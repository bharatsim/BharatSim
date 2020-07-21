const DATASOURCE_URL = '/api/dataSources';

export const url = {
  getDataUrl: (dataSource) => `${DATASOURCE_URL}/${dataSource}`,
  getHeaderUrl: (dataSource) => `${DATASOURCE_URL}/${dataSource}/headers`,
  DATA_SOURCES: `${DATASOURCE_URL}`,
};
