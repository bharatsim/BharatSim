const express = require('express');
const multer = require('multer');
const request = require('supertest');

const dashboardRoutes = require('../../src/controller/dashboardController');
const dashboardService = require('../../src/services/dashboardService');

jest.mock('../../src/services/dashboardService');

const TEST_FILE_UPLOAD_PATH = './test/testUpload/';

const app = express();
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(multer({ dest: TEST_FILE_UPLOAD_PATH }).single('datafile'));
app.use('/dashboard', dashboardRoutes);

describe('dashboardController', function () {
  describe('Post /', function () {
    it('should save dashboard data into database', async function () {
      await request(app).post('/dashboard/').send({ dashboardData: 'Data' }).expect(200);

      expect(dashboardService.saveDashboard).toHaveBeenCalledWith('Data');
    });

    it('should save dashboard data into database and return the id', async function () {
      dashboardService.saveDashboard.mockResolvedValue({ dashboardId: '_id' });

      await request(app)
        .post('/dashboard/')
        .send({ dashboardData: 'Data' })
        .expect(200)
        .expect({ dashboardId: '_id' });
    });
  });
  describe('Get /', function () {
    it('should save dashboard data into database', async function () {
      await request(app).post('/dashboard/').send({ dashboardData: 'Data' }).expect(200);

      expect(dashboardService.saveDashboard).toHaveBeenCalledWith('Data');
    });

    it('should save dashboard data into database and return the id', async function () {
      dashboardService.saveDashboard.mockResolvedValue({ dashboardId: '_id' });

      await request(app)
        .post('/dashboard/')
        .send({ dashboardData: 'Data' })
        .expect(200)
        .expect({ dashboardId: '_id' });
    });
  });
});
