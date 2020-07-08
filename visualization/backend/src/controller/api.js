const router = require('express').Router();
const dataSourceMetadataService = require('../services/dataSourceMetadataService.js');

router.get('/data', function (req, res) {
  const { columns } = req.query;
  res.json(dataSourceMetadataService.getData(columns));
});

router.post('/headers', function (req, res) {
  res.json(dataSourceMetadataService.getHeaders());
});

router.get('/dataSources', async function (req, res) {
  const data = await dataSourceMetadataService.getDataSources();
  res.json(data);
});

module.exports = router;
