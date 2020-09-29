const express = require('express');
const request = require('supertest');

const projectRoutes = require('../../src/controller/projectController');
const projectService = require('../../src/services/projectService');
const InvalidInputException = require('../../src/exceptions/InvalidInputException');

jest.mock('../../src/services/projectService');

const app = express();
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use('/projects', projectRoutes);

describe('ProjectController', function () {
  describe('Post /', function () {
    it('should create new project for given config', async function () {
      projectService.addNewProject.mockResolvedValue({ projectId: '_id' });

      await request(app).post('/projects').send({ projectData: 'Data' }).expect(200);

      expect(projectService.addNewProject).toHaveBeenCalledWith('Data');
    });
    it('should throw invalid input exception while creating new project with invalid data', async function () {
      projectService.addNewProject.mockRejectedValue(new InvalidInputException('Message'));

      await request(app)
        .post('/projects/')
        .send({ projectData: 'Data' })
        .expect(400)
        .expect({ errorMessage: 'Invalid Input - Message' });
    });
    it('should throw technical error for technical failure', async function () {
      projectService.addNewProject.mockRejectedValue(new Error('Message'));

      await request(app)
        .post('/projects')
        .send({ projectData: 'Data' })
        .expect(500)
        .expect({ errorMessage: 'Technical error Message' });
    });
  });
  describe('Put /', function () {
    it('should update existing project with given config', async function () {
      projectService.updateProject.mockResolvedValue({ projectId: '_id' });

      await request(app).put('/projects').send({ projectData: 'Data' }).expect(200);

      expect(projectService.updateProject).toHaveBeenCalledWith('Data');
    });
    it('should throw invalid input exception while saving already existing project', async function () {
      projectService.updateProject.mockRejectedValue(new InvalidInputException('Message'));

      await request(app)
        .put('/projects/')
        .send({ projectData: 'Data' })
        .expect(400)
        .expect({ errorMessage: 'Invalid Input - Message' });
    });
    it('should throw technical error for technical failure', async function () {
      projectService.updateProject.mockRejectedValue(new Error('Message'));

      await request(app)
        .put('/projects')
        .send({ projectData: 'Data' })
        .expect(500)
        .expect({ errorMessage: 'Technical error Message' });
    });
  });

  describe('Get /projects/', function () {
    it('should  fetch all the saved projects list', async function () {
      projectService.getAllProjects.mockResolvedValueOnce({ projects: {} });

      await request(app).get('/projects/').expect(200);

      expect(projectService.getAllProjects).toHaveBeenCalled();
    });
    it('should throw and technical error for any failure', async function () {
      projectService.getAllProjects.mockRejectedValueOnce(new Error('Message'));

      await request(app)
        .get('/projects/')
        .expect(500)
        .expect({ errorMessage: 'Technical error Message' });
    });
    it('should  project with matching Id', async function () {
      projectService.getProject.mockResolvedValueOnce({ project: {} });

      await request(app).get('/projects/id').expect(200);

      expect(projectService.getProject).toHaveBeenCalledWith('id');
    });
    it('should throw technical error for any failure', async function () {
      projectService.getProject.mockRejectedValue(new Error('Message'));

      await request(app)
        .get('/projects/id')
        .expect(500)
        .expect({ errorMessage: 'Technical error Message' });

      expect(projectService.getProject).toHaveBeenCalledWith('id');
    });
  });
});
