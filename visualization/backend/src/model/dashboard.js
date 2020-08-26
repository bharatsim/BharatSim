const mongoose = require('mongoose');

const { Schema } = mongoose;
const widgetSchema = new Schema(
  {
    layout: { h: Number, i: String, w: Number, x: Number, y: Number },
    dataSource: String,
    configs: Object,
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
  },
  { collection: 'dashboard' },
);

module.exports = mongoose.model('dashboard', dashboard);
