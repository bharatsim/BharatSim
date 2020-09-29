const Project = require('../../src/model/project');
const ProjectRepository = require('../../src/repository/projectRepository');

const parseMongoDBResult = (result) => JSON.parse(JSON.stringify(result));
const ProjectModel = require('../../src/model/project');

const dbHandler = require('../db-handler');

const projectConfig = { name: 'project1' };
const projectConfig1 = { name: 'project2' };
const projectConfig2 = { name: 'project3' };

describe('ProjectRepository', function () {
  beforeAll(async () => {
    await dbHandler.connect();
  });
  afterEach(async () => {
    await dbHandler.clearDatabase();
  });
  afterAll(async () => {
    await dbHandler.closeDatabase();
  });
  it('should add new project', async function () {
    const { _id } = await ProjectRepository.insert(projectConfig);
    expect(parseMongoDBResult(await Project.findOne({ _id }, { __v: 0, _id: 0 }))).toEqual({
      name: 'project1',
    });
  });
  it('should get list of all the saved projects', async function () {
    await ProjectRepository.insert(projectConfig);
    await ProjectRepository.insert(projectConfig1);
    await ProjectRepository.insert(projectConfig2);
    const allProjects = await ProjectRepository.getAll();
    expect(allProjects.length).toEqual(3);
  });
  it('should get project with matching id', async function () {
    const { _id } = await ProjectRepository.insert(projectConfig);
    const project = parseMongoDBResult(await ProjectRepository.getOne(_id));
    expect(project).toEqual({ _id: _id.toString(), name: 'project1' });
  });
  it('should return null if  project with matching id is not available', async function () {
    await ProjectRepository.insert(projectConfig);
    const project = parseMongoDBResult(await ProjectRepository.getOne('5f6c7a827b048512641dabd7'));
    expect(project).toEqual(null);
  });
  it('should update the existing project', async function () {
    const projectModel1 = new ProjectModel(projectConfig);
    const { _id } = await projectModel1.save();

    await ProjectRepository.update(_id, { name: 'new name' });
    const updatedProject = await ProjectModel.findOne({ _id }, { __v: 0 });

    expect(parseMongoDBResult(updatedProject)).toEqual({ _id: _id.toString(), name: 'new name' });
  });
});
