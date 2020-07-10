const mongoose = require('mongoose');

const { Schema } = mongoose;
const datasourceMetadata = new Schema({
  name: {
    type: String,
    unique: true,
    useCreateIndex: true,
    required: true,
  },
  dataSourceSchema: Object,
});

const DataSourceMetadata = mongoose.model('dataSourceMetadata', datasourceMetadata);

module.exports = DataSourceMetadata;
