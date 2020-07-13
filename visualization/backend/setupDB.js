/* eslint-disable no-console */
const mongoose = require('mongoose');
const { DATABASE_URL } = require('./config');

mongoose.connect(DATABASE_URL, { useNewUrlParser: true, useUnifiedTopology: true });
mongoose.set('useCreateIndex', true);

const db = mongoose.connection;
db.on('error', function () {
  console.log('#### database - connection unsuccessful ####');
});
db.once('open', function () {
  console.log('#### database - connection successful ####');
});
