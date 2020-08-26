const express = require('express');
const multer = require('multer');
const morgan = require('morgan');

const LOGGER_FORMAT_STRING = ':method :url :status :param[id] :res[content-length] - :response-time ms';
const apiRoutes = require('./src/controller/api.js');
require('./setupDB');

const FILE_UPLOAD_PATH = './uploads/';

morgan.token('param', function (req, res, param) {
  return req.params[param];
});

const app = express();

app.use(morgan(LOGGER_FORMAT_STRING));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(multer({ dest: FILE_UPLOAD_PATH }).single('datafile'));

const port = 3005;

app.get('/', (req, res) => res.send('Hello World!'));

app.use('/api', apiRoutes);

// eslint-disable-next-line no-console
app.listen(port, () => console.log(`Example app listening at http://localhost:${port}`));
