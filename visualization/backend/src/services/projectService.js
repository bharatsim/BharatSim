const InvalidInputException = require('../exceptions/InvalidInputException');

const { getAll, insert, getOne, update } = require('../repository/projectRepository');

async function addNewProject(projectData) {
  try {
    const { _id } = await insert(projectData);
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
  const project = await getOne(projectId);
  return { project };
}

async function updateProject({ id, ...projectData }) {
  try {
    await update(id, projectData);
    return { projectId: id };
  } catch (e) {
    throw new InvalidInputException('Error while updating project');
  }
}

module.exports = {
  getAllProjects,
  addNewProject,
  getProject,
  updateProject,
};
