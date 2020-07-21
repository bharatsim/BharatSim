const mongoose = require('mongoose');

const dataSourceMetadata = [
  {
    name: 'model_1',
    dataSourceSchema: {
      hour: 'number',
      susceptible: 'number',
    },
  },
  {
    name: 'model_2',
    dataSourceSchema: {
      hour_: 'number',
      susceptible_: 'number',
    },
  },
];

const model1Model = (modelName) => {
  try {
    return mongoose.model(modelName);
  } catch (e) {
    return mongoose.model(modelName, {
      hour: 'number',
      susceptible: 'number',
    });
  }
};

const model1 = [
  { hour: 0, susceptible: 1 },
  { hour: 1, susceptible: 2 },
  { hour: 2, susceptible: 3 },
  { hour: 3, susceptible: 4 },
  { hour: 4, susceptible: 5 },
];

module.exports = {
  dataSourceMetadata,
  model1,
  model1Model,
};
