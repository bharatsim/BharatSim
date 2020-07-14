const DATASOURCE_URL = '/api/dataSources';

export const url = {
  getDataUrl: (dataSource) => `${DATASOURCE_URL}/${dataSource}/data`,
  getHeaderUrl: (dataSource) => `${DATASOURCE_URL}/${dataSource}/headers`,
  DATA_SOURCES: `${DATASOURCE_URL}`,
};
