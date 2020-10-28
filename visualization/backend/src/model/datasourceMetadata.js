const mongoose = require('mongoose');

const { Schema } = mongoose;
const datasourceMetadata = new Schema(
  {
    name: {
      type: String,
      required: true,
    },
    dataSourceSchema: Object,
    dashboardId: {
      type: Schema.Types.ObjectId,
      ref: 'dashboard',
      required: true,
    },
  },
  { collection: 'datasourceMetadata' },
);

const DatasourceMetadata = mongoose.model('datasourceMetadata', datasourceMetadata);
module.exports = DatasourceMetadata;
