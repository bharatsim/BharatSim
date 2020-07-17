const dataSourceMetadataRepository = require('../repository/datasourceMetadataRepository');
const dataSourceRepository = require('../repository/datasourceRepository');
const { parseCSV } = require('../utils/csvParser');
const { createSchema } = require('../utils/dbUtils');
const modelCreator = require('../utils/modelCreator');

async function insertMetadata(fileName, schema) {
  return dataSourceMetadataRepository.insert({ name: fileName, dataSourceSchema: schema });
}

async function insertCSVData(collectionId, schema, data) {
  const DatasourceModel = modelCreator.createModel(collectionId, schema);
  await dataSourceRepository.insert(DatasourceModel, data);
}

async function uploadCsv({ path, originalname: fileName }) {
  const { data } = parseCSV(path);
  const schema = createSchema(data[0]);
  const { _id: collectionId } = await insertMetadata(fileName, schema);
  await insertCSVData(collectionId.toString(), schema, data);
}
module.exports = { uploadCsv };
