const express = require('express');
const request = require('supertest');
const multer = require('multer');

const dbHandler = require('../db-handler');
const datasourcesRoutes = require('../../src/controller/datasourcesController');
const dashboardRoutes = require('../../src/controller/dashboardController');
const dashboardModel = require('../../src/model/dashboard');
const { parseDBObject } = require('../../src/utils/dbUtils');

const TEST_FILE_UPLOAD_PATH = './uploads/';

const widget = {
  layout: { h: 1, i: 'test', w: 2, x: 1, y: 3 },
  dataSource: 'datasource',
  config: { xAxis: 'xCol', yAxis: 'ycol' },
  chartType: 'chartType',
};

const dashboardData = {
  name: 'dashboard1',
  widgets: [widget],
  layout: [],
  count: 0,
};

describe('Integration test for dashboard api', () => {
  const app = express();
  app.use(express.json());
  app.use(express.urlencoded({ extended: true }));
  app.use(multer({ dest: TEST_FILE_UPLOAD_PATH }).single('datafile'));
  app.use('/datasources', datasourcesRoutes);
  app.use('/dashboard', dashboardRoutes);
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
  describe('POST /dashboard', function () {
    it('should save dashboard to database', async function () {
      const response = await request(app).post('/dashboard').send({ dashboardData }).expect(200);
      const { dashboardId } = response.body;
      const dashboardRow = parseDBObject(
        await dashboardModel.findOne({ _id: dashboardId }, { __v: 0, _id: 0 }),
      );
      expect(dashboardRow).toEqual(dashboardData);
    });
  });

  describe('Get /dashboard', function () {
    it('should get all dashboards from database', async function () {
      dashboardModel.insertMany([dashboardData]);
      const response = await request(app).get('/dashboard').expect(200);
      expect(response.body.dashboards.length).toEqual(1);
    });
  });
});
