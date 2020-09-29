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

async function update(id, projectData) {
  return ProjectModel.updateOne({ _id: id }, projectData);
}

module.exports = {
  getAll,
  insert,
  getOne,
  update,
};
