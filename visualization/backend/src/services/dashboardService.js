const { insert, update } = require('../repository/dashboardRepository');

async function saveDashboard(dashboardData) {
  const { dashboardId, ...dashboardConfigs } = dashboardData;
  if (dashboardId) {
    await update(dashboardId, dashboardConfigs);
    return { dashboardId };
  }
  const { _id } = await insert(dashboardConfigs);
  return { dashboardId: _id };
}

module.exports = { saveDashboard };
