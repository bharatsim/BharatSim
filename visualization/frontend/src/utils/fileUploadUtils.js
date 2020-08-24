import csvParser from 'papaparse';
import dataTypesMapping from '../constants/dataTypesMapping';
import csvParserConfig from '../config/csvParserConfig';

const fileUploadedStatus = {
  LOADING: 'loading',
  SUCCESS: 'success',
  ERROR: 'error',
};

function getStatusAndMessageFor(fileUploadStatus, fileName) {
  return {
    [fileUploadedStatus.ERROR]: {
      status: fileUploadedStatus.ERROR,
      message: `Error occurred while unloading ${fileName}`,
    },
    [fileUploadedStatus.SUCCESS]: {
      status: fileUploadedStatus.SUCCESS,
      message: `${fileName} successfully uploaded`,
    },
    [fileUploadedStatus.LOADING]: {
      status: fileUploadedStatus.LOADING,
      message: `uploading ${fileName}`,
    },
  }[fileUploadStatus];
}
function createSchema(row) {
  return Object.keys(row).reduce((acc, element) => {
    acc[element] = dataTypesMapping[typeof row[element]];
    return acc;
  }, {});
}

function parseCsv(csvFile, onComplete) {
  csvParser.parse(csvFile, {
    ...csvParserConfig,
    complete: onComplete,
  });
}

export { fileUploadedStatus, getStatusAndMessageFor, createSchema, parseCsv };
