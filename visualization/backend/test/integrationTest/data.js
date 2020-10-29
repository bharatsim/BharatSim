const mongoose = require('mongoose');

const dataSourceMetadata = [
  {
    name: 'model_1',
    dataSourceSchema: {
      hour: 'number',
      susceptible: 'number',
    },
    fileSize: 123,
    fileType: 'csv',
    dashboardId: '313233343536373839303137',
  },
  {
    name: 'model_2',
    dataSourceSchema: {
      hour_: 'number',
      susceptible_: 'number',
    },
    fileSize: 123,
    fileType: 'csv',
    dashboardId: '313233343536373839303137',
  },
];

const model1Model = (modelName) => {
  try {
    return mongoose.model(modelName);
  } catch (e) {
    return mongoose.model(modelName, {
      hour: 'number',
      susceptible: 'number',
      city: 'string',
    });
  }
};

const model1 = [
  { hour: 0, susceptible: 1, city: 'pune' },
  { hour: 1, susceptible: 2, city: 'pune' },
  { hour: 2, susceptible: 3, city: 'pune' },
  { hour: 3, susceptible: 4, city: 'pune' },
  { hour: 4, susceptible: 5, city: 'pune' },
];

module.exports = {
  dataSourceMetadata,
  model1,
  model1Model,
};
