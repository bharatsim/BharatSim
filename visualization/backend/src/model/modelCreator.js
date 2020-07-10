const mongoose = require('mongoose');

const { Schema } = mongoose;

function createModel(modelName, modelSkeleton) {
  return mongoose.model(modelName, new Schema(modelSkeleton));
}

module.exports = {
  createModel,
};
