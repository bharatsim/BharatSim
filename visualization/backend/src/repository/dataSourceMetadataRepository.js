const DataSourceMetadataRepository = require('../model/dataSourceMetadata');
const DataSourceNotFoundException = require('../exceptions/DataSourceNotFoundException');

async function getDataSourceNames() {
  return DataSourceMetadataRepository.find({}, { _id: 0 })
    .select('name')
    .then((data) => data);
}

async function getDataSourceSchema(dataSourceName) {
  return DataSourceMetadataRepository.findOne({ name: dataSourceName }, { _id: 0 })
    .select('dataSourceSchema')
    .then((data) => {
      if (!data) {
        throw new DataSourceNotFoundException(dataSourceName);
      }
      return data;
    });
}

module.exports = {
  getDataSourceNames,
  getDataSourceSchema,
};
