const DataSourceMetadataRepository = require('../model/dataSourceMetadata');

async function getDataSourceNames() {
  return DataSourceMetadataRepository.find({}, { _id: 0 })
    .select('name')
    .then((data) => data);
}

module.exports = {
  getDataSourceNames,
};
