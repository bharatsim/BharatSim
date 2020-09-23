const express = require('express');
const request = require('supertest');

const dbHandler = require('../db-handler');
const projectRoutes = require('../../src/controller/projectController');
const { parseDBObject } = require('../../src/utils/dbUtils');
const projectModel = require('../../src/model/project');

const projectData = {
  name: 'project1',
};

describe('Integration test for project api', () => {
  const app = express();
  app.use(express.json());
  app.use(express.urlencoded({ extended: true }));
  app.use('/project', projectRoutes);
  beforeAll(async () => {
    await dbHandler.connect();
  });
  afterEach(async () => {
    await dbHandler.clearDatabase();
  });
  afterAll(async () => {
    await dbHandler.clearDatabase();
    await dbHandler.closeDatabase();
  });
  describe('POST /project', function () {
    it('should add new project to database', async function () {
      const response = await request(app).post('/project').send({ projectData }).expect(200);
      const { projectId } = response.body;
      const projects = parseDBObject(
        await projectModel.findOne({ _id: projectId }, { __v: 0, _id: 0 }),
      );
      expect(projects).toEqual(projectData);
    });
  });

  describe('Get /project', function () {
    it('should get all projects from database', async function () {
      projectModel.insertMany([projectData]);
      const response = await request(app).get('/project').expect(200);
      expect(response.body.projects.length).toEqual(1);
    });
  });
});
