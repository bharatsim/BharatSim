const router = require('express').Router();
const dataSourceMetadataService = require('../services/datasourceMetadataService.js');
const dataSourceService = require('../services/datasourceService.js');

router.get('/datasources/:name/data', function (req, res) {
  const { columns } = req.query;
  const { name: dataSourceName } = req.params;
  res.json(dataSourceService.getData(dataSourceName, columns));
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
