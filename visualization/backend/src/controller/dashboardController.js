const router = require('express').Router();

const InvalidInputException = require('../exceptions/InvalidInputException');
const technicalErrorException = require('../exceptions/TechnicalErrorException');
const {
  saveDashboard,
  getAllDashboards,
  insertDashboard,
} = require('../services/dashboardService');

// TODO: Refactor APIS for new dashboard

router.post('/', async function (req, res) {
  const { dashboardData } = req.body;
  saveDashboard(dashboardData)
    .then((dashboardId) => {
      res.send(dashboardId);
    })
    .catch((err) => {
      if (err instanceof InvalidInputException) {
        res.status(400).send({ errorMessage: err.message });
      } else {
        technicalErrorException(err, res);
      }
    });
});

router.post('/create-new', async function (req, res) {
  const { dashboardData } = req.body;
  insertDashboard(dashboardData)
    .then((dashboardId) => {
      res.send(dashboardId);
    })
    .catch((err) => {
      if (err instanceof InvalidInputException) {
        res.status(400).send({ errorMessage: err.message });
      } else {
        technicalErrorException(err, res);
      }
    });
});

router.get('/', async function (req, res) {
  const { columns, ...filters } = req.query;
  getAllDashboards(filters, columns)
    .then((dashboards) => {
      res.send(dashboards);
    })
    .catch((err) => {
      technicalErrorException(err, res);
    });
});

module.exports = router;
