const router = require('express').Router();
const { saveDashboard, getAllDashboards } = require('../services/dashboardService');

router.post('/', async function (req, res) {
  const { dashboardData } = req.body;
  const result = await saveDashboard(dashboardData);
  res.send(result);
});

router.get('/', async function (req, res) {
  const result = await getAllDashboards();
  res.send(result);
});

module.exports = router;
