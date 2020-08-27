const { insert, update, getAll } = require('../repository/dashboardRepository');

async function saveDashboard(dashboardData) {
  const { dashboardId, ...dashboardConfigs } = dashboardData;
  if (dashboardId) {
    await update(dashboardId, dashboardConfigs);
    return { dashboardId };
  }
  const { _id } = await insert(dashboardConfigs);
  return { dashboardId: _id };
}

async function getAllDashboards() {
  const dashboards = await getAll();
  return dashboards;
}

module.exports = { saveDashboard, getAllDashboards };
