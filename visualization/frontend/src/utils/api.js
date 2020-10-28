import { contentTypes, httpMethods } from '../constants/fetch';
import { url as serviceURL } from './url';
import { fetchData, uploadData } from './fetch';

function headerBuilder({ contentType }) {
  return {
    'content-type': contentType,
  };
}

function formDataBuilder(data) {
  const formData = new FormData();
  data.forEach(({ name, value }) => formData.append(name, value));
  return formData;
}

const api = {
  saveDashboard: async ({ widgets, layout, dashboardId, name, count }) => {
    return uploadData({
      url: serviceURL.DASHBOARD_URL,
      headers: headerBuilder({ contentType: contentTypes.JSON }),
      data: JSON.stringify({
        dashboardData: { widgets, layout, dashboardId, name, count },
      }),
    });
  },

  addNewDashboard: async ({ name, projectId }) => {
    return uploadData({
      url: serviceURL.INSERT_DASHBOARD,
      headers: headerBuilder({ contentType: contentTypes.JSON }),
      data: JSON.stringify({
        dashboardData: { widgets: [], layout: [], name, count: 0, projectId },
      }),
    });
  },

  getAllDashBoardByProjectId: async (projectId) =>
    fetchData({ url: serviceURL.DASHBOARD_URL, query: { projectId, columns: ['name', '_id'] } }),

  getAllDashBoard: async () => fetchData({ url: serviceURL.DASHBOARD_URL }),

  uploadFileAndSchema: async ({ file, schema, dashboardId }) =>
    uploadData({
      url: serviceURL.DATA_SOURCES,
      data: formDataBuilder([
        { name: 'datafile', value: file },
        { name: 'schema', value: JSON.stringify(schema) },
        { name: 'dashboardId', value: dashboardId },
      ]),
      headers: headerBuilder({ contentType: contentTypes.FILE }),
    }),

  getCsvHeaders: async (dataSourceId) => fetchData({ url: serviceURL.getHeaderUrl(dataSourceId) }),

  getDatasources: async () => fetchData({ url: serviceURL.DATA_SOURCES }),

  getData: async (datasource, columns) =>
    fetchData({ url: serviceURL.getDataUrl(datasource), query: { columns } }),

  saveProject: async ({ id, ...data }) => {
    const requestObject = {
      url: serviceURL.PROJECT_URL,
      headers: headerBuilder({ contentType: contentTypes.JSON }),
    };
    if (id) {
      return uploadData({
        ...requestObject,
        data: JSON.stringify({ projectData: { ...data, id } }),
        method: httpMethods.PUT,
      });
    }
    return uploadData({
      ...requestObject,
      data: JSON.stringify({ projectData: { ...data } }),
      method: httpMethods.POST,
    });
  },

  getProjects: async () => {
    return fetchData({
      url: serviceURL.PROJECT_URL,
    });
  },
  getProject: async (id) => {
    return fetchData({
      url: serviceURL.getProjectUrl(id),
    });
  },
};

export { api };
