const express = require('express');
const multer = require('multer');
const request = require('supertest');

const dashboardRoutes = require('../../src/controller/dashboardController');
const dashboardService = require('../../src/services/dashboardService');
const InvalidInputException = require('../../src/exceptions/InvalidInputException');

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
      dashboardService.saveDashboard.mockResolvedValue({ dashboardId: '_id' });
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
    it('should throw invalid input exception while saving invalid data', async function () {
      dashboardService.saveDashboard.mockRejectedValue(new InvalidInputException('Message'));

      await request(app)
        .post('/dashboard/')
        .send({ dashboardData: 'Data' })
        .expect(400)
        .expect({ errorMessage: 'Invalid Input - Message' });
    });
    it('should throw technical error for technical failure', async function () {
      dashboardService.saveDashboard.mockRejectedValue(new Error('Message'));

      await request(app)
        .post('/dashboard/')
        .send({ dashboardData: 'Data' })
        .expect(500)
        .expect({ errorMessage: 'Technical error Message' });
    });
  });

  describe('Get /dashboard/', function () {
    it('should save dashboard data into database', async function () {
      dashboardService.getAllDashboards.mockResolvedValueOnce({ dashboards: {} });
      await request(app).get('/dashboard/').expect(200);
      expect(dashboardService.getAllDashboards).toHaveBeenCalled();
    });
    it('should throw and technical error for any failure', async function () {
      dashboardService.getAllDashboards.mockRejectedValueOnce(new Error('Message'));
      await request(app)
        .get('/dashboard/')
        .expect(500)
        .expect({ errorMessage: 'Technical error Message' });
    });
  });
});
