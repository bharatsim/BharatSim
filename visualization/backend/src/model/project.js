const mongoose = require('mongoose');

const { Schema } = mongoose;

const Project = new Schema({
  name: {
    type: String,
    required: true,
  },
});

module.exports = mongoose.model('project', Project);
