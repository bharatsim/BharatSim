const router = require('express').Router();

const InvalidInputException = require('../exceptions/InvalidInputException');
const technicalErrorException = require('../exceptions/TechnicalErrorException');
const { saveDashboard, getAllDashboards } = require('../services/dashboardService');

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

router.get('/', async function (req, res) {
  getAllDashboards()
    .then((dashboards) => {
      res.send(dashboards);
    })
    .catch((err) => {
      technicalErrorException(err, res);
    });
});

module.exports = router;
