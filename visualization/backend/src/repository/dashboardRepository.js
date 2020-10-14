const DashboardModel = require('../model/dashboard');

async function insert(dashboardConfigs) {
  const dashboardModel = new DashboardModel(dashboardConfigs);
  return dashboardModel.save();
}

async function update(dashboardId, dashboardConfigs) {
  return DashboardModel.updateOne({ _id: dashboardId }, dashboardConfigs);
}

async function getAll(filter, projectedColumns) {
  return DashboardModel.find(filter, projectedColumns);
}

module.exports = {
  insert,
  update,
  getAll,
};
