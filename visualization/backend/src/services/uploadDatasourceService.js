const fs = require('fs');

const dataSourceMetadataRepository = require('../repository/datasourceMetadataRepository');
const dataSourceRepository = require('../repository/datasourceRepository');
const { validateAndParseCSV } = require('../utils/csvParser');
const { createSchema } = require('../utils/dbUtils');
const modelCreator = require('../utils/modelCreator');
const InvalidInputException = require('../exceptions/InvalidInputException');
const { fileTypes, MAX_FILE_SIZE } = require('../constants/fileTypes');

async function insertMetadata(fileName, schema) {
  return dataSourceMetadataRepository.insert({ name: fileName, dataSourceSchema: schema });
}

async function insertCSVData(collectionId, schema, data) {
  try {
    const DatasourceModel = modelCreator.createModel(collectionId, schema);
    await dataSourceRepository.insert(DatasourceModel, data);
  } catch (error) {
    await dataSourceMetadataRepository.deleteDatasource(collectionId);
    throw new InvalidInputException('Error while uploading csv file data');
  }
}

function deleteUploadedFile(filePath) {
  if (fs.existsSync(filePath)) {
    fs.rmdirSync(filePath, { recursive: true });
  }
}

function validateFileAndThrowException(fileType, fileSize) {
  if (fileType !== fileTypes.CSV) {
    throw new InvalidInputException('File type does not match');
  }
  if (!fileSize || fileSize > MAX_FILE_SIZE) {
    throw new InvalidInputException('File is too large');
  }
}

async function uploadCsv(csvFile) {
  const { path, originalname: fileName, mimetype: fileType, size } = csvFile;
  validateFileAndThrowException(fileType, size);
  const data = validateAndParseCSV(path);
  const schema = createSchema(data[0]);
  const { _id: collectionId } = await insertMetadata(fileName, schema);
  await insertCSVData(collectionId.toString(), schema, data);
  return { collectionId };
}
module.exports = { uploadCsv, deleteUploadedFile };
