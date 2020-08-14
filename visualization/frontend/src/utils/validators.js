const isAbsent = (value) => {
  return value === null || value === undefined || value === '';
};

function isNotAxisTypeNumber(value) {
  return value.type !== 'number';
}

function isEmptyObject(value) {
  return Object.keys(value).length === 0;
}

const xAxisValidator = (value = '') => {
  if (isAbsent(value)) {
    return 'Please select value for x axis';
  }
  return '';
};

const yAxisValidator = (value = {}) => {
  if (isAbsent(value)) {
    return 'Please select value for y axis';
  }
  if (isEmptyObject(value)) {
    return 'Please select value for y axis';
  }
  if (isNotAxisTypeNumber(value)) {
    return 'Please select number type option';
  }
  return '';
};

const datasourceValidator = (value = '') => {
  if (isAbsent(value)) {
    return 'Please select data source';
  }
  return '';
};

export { datasourceValidator, xAxisValidator, yAxisValidator };
