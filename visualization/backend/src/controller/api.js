const router = require('express').Router();

const dataSourceMetadataService = require('../services/datasourceMetadataService.js');
const dataSourceService = require('../services/datasourceService.js');
const uploadDatasourceService = require('../services/uploadDatasourceService.js');
const DataSourceNotFoundException = require('../exceptions/DatasourceNotFoundException');
const ColumnsNotFoundException = require('../exceptions/ColumnsNotFoundException');
const technicalErrorException = require('../exceptions/TechnicalErrorException');
const InvalidInputException = require('../exceptions/InvalidInputException');

router.get('/datasources/:id', async function (req, res) {
  const { columns } = req.query;
  const { id: dataSourceId } = req.params;
  dataSourceService
    .getData(dataSourceId, columns)
    .then((data) => res.json(data))
    .catch((err) => {
      if (err instanceof DataSourceNotFoundException) {
        res.status(404).send({ errorMessage: err.message });
      } else if (err instanceof ColumnsNotFoundException) {
        res.status(200).end();
      } else {
        technicalErrorException(err, res);
      }
    });
});

router.get('/datasources/:id/headers', function (req, res) {
  const { id: dataSourceId } = req.params;
  dataSourceMetadataService
    .getHeaders(dataSourceId)
    .then((headers) => res.json(headers))
    .catch((err) => {
      if (err instanceof DataSourceNotFoundException) {
        res.status(404).send({ errorMessage: err.message });
      } else {
        technicalErrorException(err, res);
      }
    });
});

router.get('/datasources', async function (req, res) {
  dataSourceMetadataService
    .getDataSources()
    .then((data) => res.json(data))
    .catch((err) => technicalErrorException(err, res));
});

router.post('/datasources', async function (req, res) {
  uploadDatasourceService
    .uploadCsv(req.file, req.body.schema)
    .then((data) => res.json(data))
    .catch((err) => {
      if (err instanceof InvalidInputException) {
        res.status(400).send({ errorMessage: err.message });
      } else {
        technicalErrorException(err, res);
      }
    })
    .finally(() => {
      uploadDatasourceService.deleteUploadedFile(req.file.path);
    });
});
module.exports = router;
