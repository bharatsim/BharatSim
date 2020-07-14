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

const model1Model = mongoose.model('model_1', {
  hour: 'number',
  susceptible: 'number',
});

const model1 = [
  { hour: 0, susceptible: 1 },
  { hour: 1, susceptible: 2 },
  { hour: 2, susceptible: 3 },
  { hour: 3, susceptible: 4 },
  { hour: 4, susceptible: 5 },
];

const model2 = [
  { hour_: 0, susceptible_: 1 },
  { hour_: 1, susceptible_: 2 },
  { hour_: 2, susceptible_: 3 },
  { hour_: 3, susceptible_: 4 },
  { hour_: 4, susceptible_: 5 },
];

module.exports = {
  dataSourceMetadata,
  model1,
  model2,
  model1Model,
};
