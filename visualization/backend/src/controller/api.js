const router = require('express').Router();
const dataSourceMetadataService = require('../services/dataSourceMetadataService.js');

router.get('/data', function (req, res) {
  const { columns } = req.query;
  res.json(dataSourceMetadataService.getData(columns));
});

router.get('/datasources/:name/headers', function (req, res) {
  const { name: dataSourceName } = req.params;
  dataSourceMetadataService
    .getHeaders(dataSourceName)
    .then((headers) => res.json(headers))
    .catch((err) => {
      res.status(404);
      res.send({ errorMessage: err.message });
    });
});

router.get('/datasources', async function (req, res) {
  const data = await dataSourceMetadataService.getDataSources();
  res.json(data);
});

module.exports = router;
