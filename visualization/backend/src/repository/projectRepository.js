const ProjectModel = require('../model/project');

async function getAll() {
  return ProjectModel.find({}, { __v: 0 });
}

async function getOne(projectId) {
  return ProjectModel.findOne({ _id: projectId }, { __v: 0 });
}

async function insert(projectConfig) {
  const projectModel = new ProjectModel(projectConfig);
  return projectModel.save();
}

module.exports = {
  getAll,
  insert,
  getOne,
};
