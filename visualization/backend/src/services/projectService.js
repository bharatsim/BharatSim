const InvalidInputException = require('../exceptions/InvalidInputException');

const { getAll, insert, getOne } = require('../repository/projectRepository');

async function addNewProject(projectConfig) {
  try {
    const { _id } = await insert(projectConfig);
    return { projectId: _id };
  } catch (e) {
    throw new InvalidInputException('Error while creating new project');
  }
}

async function getAllProjects() {
  const projects = await getAll();
  return { projects };
}

async function getProject(projectId) {
  const projects = await getOne(projectId);
  return { projects };
}

module.exports = {
  getAllProjects,
  addNewProject,
  getProject,
};
