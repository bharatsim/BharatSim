const mongoose = require('mongoose');

const { Schema } = mongoose;

function createModel(modelName, modelSkeleton) {
  try {
    return mongoose.model(modelName);
  } catch (error) {
    return mongoose.model(modelName, new Schema(modelSkeleton, { collection: modelName }));
  }
}

module.exports = {
  createModel,
};
