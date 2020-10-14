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

  describe('Post /create-new', function () {
    it('should insert new dashboard', async function () {
      dashboardService.insertDashboard.mockResolvedValue({ dashboardId: '_id' });
      await request(app).post('/dashboard/create-new').send({ dashboardData: 'Data' }).expect(200);

      expect(dashboardService.insertDashboard).toHaveBeenCalledWith('Data');
    });

    it('should throw invalid input exception while inserting invalid data', async function () {
      dashboardService.insertDashboard.mockRejectedValue(new InvalidInputException('Message'));

      await request(app)
        .post('/dashboard/create-new')
        .send({ dashboardData: 'Data' })
        .expect(400)
        .expect({ errorMessage: 'Invalid Input - Message' });
    });
    it('should throw technical error for technical failure', async function () {
      dashboardService.insertDashboard.mockRejectedValue(new Error('Message'));

      await request(app)
        .post('/dashboard/create-new')
        .send({ dashboardData: 'Data' })
        .expect(500)
        .expect({ errorMessage: 'Technical error Message' });
    });
  });

  describe('Get /dashboard/', function () {
    it('should geta all dashboard ', async function () {
      dashboardService.getAllDashboards.mockResolvedValueOnce({ dashboards: {} });

      await request(app).get('/dashboard/').expect(200);

      expect(dashboardService.getAllDashboards).toHaveBeenCalledWith({}, undefined);
    });

    it('should get dashboard data by project id filter', async function () {
      dashboardService.getAllDashboards.mockResolvedValueOnce({ dashboards: {} });
      await request(app).get('/dashboard?projectId=5f75ce5999399c14af5a2845').expect(200);

      expect(dashboardService.getAllDashboards).toHaveBeenCalledWith(
        { projectId: '5f75ce5999399c14af5a2845' },
        undefined,
      );
    });

    it('should get dashboard data with projected columns and filter', async function () {
      dashboardService.getAllDashboards.mockResolvedValueOnce({ dashboards: {} });
      await request(app)
        .get('/dashboard?projectId=5f75ce5999399c14af5a2845&columns[]=name&&columns[]=_id')
        .expect(200);

      expect(dashboardService.getAllDashboards).toHaveBeenCalledWith(
        { projectId: '5f75ce5999399c14af5a2845' },
        ['name', '_id'],
      );
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
