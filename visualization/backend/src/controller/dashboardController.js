const router = require('express').Router();
const { saveDashboard } = require('../services/dashboardService');

router.post('/', async function (req, res) {
  const { dashboardData } = req.body;
  const result = await saveDashboard(dashboardData);
  res.send(result);
});

module.exports = router;
