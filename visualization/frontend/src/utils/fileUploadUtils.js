import csvParser from 'papaparse';
import dataTypesMapping from '../constants/dataTypesMapping';
import csvParserConfig from '../config/csvParserConfig';
import { loaderStates } from '../hook/useInlineLoader';

function getMessage(fileUploadStatus, fileName) {
  return {
    [loaderStates.ERROR]: `Error occurred while unloading ${fileName}`,
    [loaderStates.SUCCESS]: `${fileName} successfully uploaded`,
    [loaderStates.LOADING]: `uploading ${fileName}`,
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

function resetFileInput(fileInput) {
  // eslint-disable-next-line no-param-reassign
  fileInput.value = '';
  // eslint-disable-next-line no-param-reassign
  fileInput.files = null;
}

export { getMessage, createSchema, parseCsv, resetFileInput };