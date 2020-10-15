const DATASOURCE_URL = '/api/dataSources';
const DASHBOARD_URL = '/api/dashboard';
const PROJECT_URL = '/api/projects';

export const url = {
  getDataUrl: (dataSource) => `${DATASOURCE_URL}/${dataSource}`,
  getHeaderUrl: (dataSource) => `${DATASOURCE_URL}/${dataSource}/headers`,
  getProjectUrl: (projectId) => `${PROJECT_URL}/${projectId}`,
  INSERT_DASHBOARD: `${DASHBOARD_URL}/create-new`,
  DATA_SOURCES: `${DATASOURCE_URL}`,
  DASHBOARD_URL,
  PROJECT_URL,
};
