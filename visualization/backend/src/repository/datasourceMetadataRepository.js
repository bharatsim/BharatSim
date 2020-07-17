const DataSourceMetadata = require('../model/datasourceMetadata');
const DataSourceNotFoundException = require('../exceptions/DatasourceNotFoundException');

async function getDataSourceNames() {
  return DataSourceMetadata.find({}, { _id: 0 })
    .select('name')
    .then((data) => data);
}

async function getDataSourceSchema(dataSourceName) {
  return DataSourceMetadata.findOne({ name: dataSourceName }, { _id: 0 })
    .select('dataSourceSchema')
    .then((data) => {
      if (!data) {
        throw new DataSourceNotFoundException(dataSourceName);
      }
      return data;
    });
}

async function insert({ name, dataSourceSchema }) {
  const dataSourceMetadata = new DataSourceMetadata({ name, dataSourceSchema });
  return dataSourceMetadata.save();
}

module.exports = {
  getDataSourceNames,
  getDataSourceSchema,
  insert,
};
