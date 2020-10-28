const mongoose = require('mongoose');

const { Schema } = mongoose;
const widgetSchema = new Schema(
  {
    layout: { h: Number, i: String, w: Number, x: Number, y: Number },
    dataSource: String,
    config: Object,
    chartType: String,
  },
  { _id: false },
);

const dashboard = new Schema(
  {
    name: {
      type: String,
      required: true,
    },
    widgets: [widgetSchema],
    layout: [Object],
    count: { type: Number },
    // TODO: Add Required in project id
    projectId: {
      type: Schema.Types.ObjectId,
      ref: 'project',
    },
  },
  { collection: 'dashboard' },
);

module.exports = mongoose.model('dashboard', dashboard);
