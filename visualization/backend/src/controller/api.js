const router = require('express').Router();
const csvService = require('../services/csvService.js');

router.get('/data', function (req, res) {
  const { columns } = req.query;
  res.json(csvService.getData(columns));
});

router.post('/headers', function (req, res) {
  res.json(csvService.getHeaders());
});

router.get('/dataSources', function(req, res) {
  res.json(csvService.getDataSources());
})
module.exports = router;
