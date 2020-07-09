const mongoose = require('mongoose');

const { Schema } = mongoose;
const dataSourceMetadata = new Schema({
  name: {
    type: String,
    unique: true,
    useCreateIndex: true,
    required: true,
  },
  dataSourceSchema: Object,
});

const DataSourceMetadata = mongoose.model('dataSourceMetadata', dataSourceMetadata);

module.exports = DataSourceMetadata;
