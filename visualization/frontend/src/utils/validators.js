const VALID_FILE_TYPES = ['text/csv'];
const MAX_FILE_SIZE = 10485760;

function isAbsent(value) {
  return value === null || value === undefined || value === '';
}

function isNotAxisTypeNumber(value) {
  return !(value.type === 'Number' || value.type === 'number');
}

function areAllAxisTypesNumber(value) {
  return value.some(isNotAxisTypeNumber);
}

function isEmptyArray(value) {
  return Array.isArray(value) && !value.length;
}
function xAxisValidator(value = '') {
  if (isAbsent(value)) {
    return 'Please select value for x axis';
  }
  return '';
}

function yAxisValidator(value = []) {
  if (isAbsent(value)) {
    return 'Please select valid value for y axis';
  }
  if (isEmptyArray(value)) {
    return 'Please select value for y axis';
  }
  if (areAllAxisTypesNumber(value)) {
    return 'Please select number type option';
  }
  return '';
}

function datasourceValidator(value = '') {
  if (isAbsent(value)) {
    return 'Please select data source';
  }
  return '';
}

function validateFile(file) {
  if (!file) {
    return 'Please upload valid csv file';
  }
  if (!VALID_FILE_TYPES.includes(file.type)) {
    return 'Please upload valid csv file';
  }
  if (file.size > MAX_FILE_SIZE) {
    return 'Please upload valid csv file with size less than 10MB';
  }
  return '';
}

export { datasourceValidator, xAxisValidator, yAxisValidator, validateFile };
