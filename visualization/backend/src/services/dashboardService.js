const { getProjectedColumns } = require('../utils/dbUtils');
const InvalidInputException = require('../exceptions/InvalidInputException');
const { insert, update, getAll } = require('../repository/dashboardRepository');

async function updateDashboard(dashboardId, dashboardConfigs) {
  try {
    await update(dashboardId, dashboardConfigs);
  } catch (e) {
    throw new InvalidInputException('Error while updating dashboard');
  }
}

async function insertDashboard(dashboardConfigs) {
  try {
    const { _id } = await insert(dashboardConfigs);
    return { dashboardId: _id };
  } catch (e) {
    throw new InvalidInputException('Error while inserting dashboard');
  }
}

async function saveDashboard(dashboardData) {
  const { dashboardId, ...dashboardConfigs } = dashboardData;
  if (dashboardId) {
    await updateDashboard(dashboardId, dashboardConfigs);
    return { dashboardId };
  }
  return insertDashboard(dashboardConfigs);
}

async function getAllDashboards(filters, columns) {
  const projectedColumns = getProjectedColumns(columns);
  const dashboards = await getAll(filters, projectedColumns);
  return { dashboards };
}

module.exports = { saveDashboard, insertDashboard, getAllDashboards };
