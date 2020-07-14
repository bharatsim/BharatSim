const router = require('express').Router();
const dataSourceMetadataService = require('../services/datasourceMetadataService.js');
const dataSourceService = require('../services/datasourceService.js');
const DataSourceNotFoundException = require('../exceptions/DatasourceNotFoundException');
const ColumnsNotFoundException = require('../exceptions/ColumnsNotFoundException');

router.get('/datasources/:name/data', async function (req, res) {
  const { columns } = req.query;
  const { name: dataSourceName } = req.params;
  dataSourceService
    .getData(dataSourceName, columns)
    .then((data) => res.json(data))
    .catch((err) => {
      if (err instanceof DataSourceNotFoundException) {
        res.status(404).send({ errorMessage: err.message });
      } else if (err instanceof ColumnsNotFoundException) {
        res.status(200).end();
      } else {
        res.status(500).send({ errorMessage: `Technical error ${err.message}` });
      }
    });
});

router.get('/datasources/:name/headers', function (req, res) {
  const { name: dataSourceName } = req.params;
  dataSourceMetadataService
    .getHeaders(dataSourceName)
    .then((headers) => res.json(headers))
    .catch((err) => {
      if (err instanceof DataSourceNotFoundException) {
        res.status(404).send({ errorMessage: err.message });
      } else {
        res.status(500).send({ errorMessage: `Technical error ${err.message}` });
      }
    });
});

router.get('/datasources', async function (req, res) {
  dataSourceMetadataService
    .getDataSources()
    .then((data) => res.json(data))
    .catch((err) => res.status(500).send({ errorMessage: `Technical error ${err.message}` }));
});

module.exports = router;
