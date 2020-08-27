const DATASOURCE_URL = '/api/dataSources';
const DASHBOARD_URL = '/api/dashboard';

export const url = {
  getDataUrl: (dataSource) => `${DATASOURCE_URL}/${dataSource}`,
  getHeaderUrl: (dataSource) => `${DATASOURCE_URL}/${dataSource}/headers`,
  DATA_SOURCES: `${DATASOURCE_URL}`,
  saveDashboard: DASHBOARD_URL,
};
