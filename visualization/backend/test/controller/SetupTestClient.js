const express = require('express');
const multer = require('multer');

const TEST_FILE_UPLOAD_PATH = './test/testUpload/';

const app = express();
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(multer({ dest: TEST_FILE_UPLOAD_PATH }).single('datafile'));

module.exports = app;
