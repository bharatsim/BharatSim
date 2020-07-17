const express = require('express');
const multer = require('multer');

const apiRoutes = require('./src/controller/api.js');
require('./setupDB');

const FILE_UPLOAD_PATH = './uploads/';

const app = express();
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(multer({ dest: FILE_UPLOAD_PATH }).single('datafile'));

const port = 3005;

app.get('/', (req, res) => res.send('Hello World!'));

app.use('/api', apiRoutes);

// eslint-disable-next-line no-console
app.listen(port, () => console.log(`Example app listening at http://localhost:${port}`));
