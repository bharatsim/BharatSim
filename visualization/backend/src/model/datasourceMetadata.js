const mongoose = require('mongoose');

const { Schema } = mongoose;
const datasourceMetadata = new Schema(
  {
    name: {
      type: String,
      required: true,
    },
    dataSourceSchema: {
      type: Object,
      required: true,
    },
    dashboardId: {
      type: Schema.Types.ObjectId,
      ref: 'dashboard',
      required: true,
    },
    fileType: {
      type: String,
      required: true,
    },
    fileSize: {
      type: Number,
      required: true,
    },
  },
  { collection: 'datasourceMetadata', timestamps: true },
);

const DatasourceMetadata = mongoose.model('datasourceMetadata', datasourceMetadata);
module.exports = DatasourceMetadata;
