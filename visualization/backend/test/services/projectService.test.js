const { addNewProject, getAllProjects, getProject } = require('../../src/services/projectService');
const projectRepository = require('../../src/repository/projectRepository');
const InvalidInputException = require('../../src/exceptions/InvalidInputException');

jest.mock('../../src/repository/projectRepository');

describe('Project service', function () {
  it('should add new project', async function () {
    projectRepository.insert.mockResolvedValue({ _id: 'new_id' });
    await addNewProject({ name: 'project1' });
    expect(projectRepository.insert).toHaveBeenCalledWith({ name: 'project1' });
  });
  it('should throw InvalidInputException for invalid input', async function () {
    projectRepository.insert.mockRejectedValue(new Error());

    const result = async () => {
      await addNewProject({ something: 'bad' });
    };

    await expect(result).rejects.toThrow(
      new InvalidInputException('Error while creating new project'),
    );
  });
  it('should fetch all the saved projects', async function () {
    projectRepository.getAll.mockResolvedValue({ project: [] });
    await getAllProjects();
    expect(projectRepository.getAll).toHaveBeenCalled();
  });
  it('should fetch project with matching id', async function () {
    projectRepository.getOne.mockResolvedValue({ name: 'project' });
    await getProject('_id');
    expect(projectRepository.getOne).toHaveBeenCalledWith('_id');
  });
});
