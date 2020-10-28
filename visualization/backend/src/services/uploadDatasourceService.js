const fs = require('fs');

const dataSourceMetadataRepository = require('../repository/datasourceMetadataRepository');
const dataSourceRepository = require('../repository/datasourceRepository');
const { validateAndParseCSV } = require('../utils/csvParser');
const modelCreator = require('../utils/modelCreator');
const InvalidInputException = require('../exceptions/InvalidInputException');
const { fileTypes, MAX_FILE_SIZE } = require('../constants/fileTypes');

async function insertMetadata(fileName, schema, dashboardId) {
  return dataSourceMetadataRepository.insert({
    name: fileName,
    dataSourceSchema: schema,
    dashboardId,
  });
}

async function insertCSVData(metadataId, schema, data) {
  try {
    const DatasourceModel = modelCreator.createModel(metadataId, schema);
    await dataSourceRepository.insert(DatasourceModel, data);
  } catch (error) {
    await dataSourceMetadataRepository.deleteDatasourceMetadata(metadataId);
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

async function uploadCsv(csvFile, requestBody) {
  const { schema, dashboardId } = requestBody;
  const schemaJson = JSON.parse(schema);
  const { path, originalname: fileName, mimetype: fileType, size } = csvFile;
  validateFileAndThrowException(fileType, size);
  const data = validateAndParseCSV(path);
  const { _id: metadataId } = await insertMetadata(fileName, schemaJson, dashboardId);
  await insertCSVData(metadataId.toString(), schemaJson, data);
  return { collectionId: metadataId };
}
module.exports = { uploadCsv, deleteUploadedFile };
