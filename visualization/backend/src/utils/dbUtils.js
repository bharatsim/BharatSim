const dbConstants = require('../constants/dbConstants');

function getIncludedColumnMap(columns) {
  const columnsMap = {};
  columns.forEach((col) => {
    columnsMap[col] = 1;
  });
  return columnsMap;
}

function getProjectedColumns(columns) {
  if (!columns) {
    return dbConstants.EXCLUDED_COLUMNS_V;
  }
  return getIncludedColumnMap(columns);
}

function parseDBObject(records) {
  return JSON.parse(JSON.stringify(records));
}

function changeRecordDimensionToArray(records) {
  const parsedRecords = parseDBObject(records);
  const columns = Object.keys(parsedRecords[0]);
  return columns.reduce((acc, column) => {
    acc[column] = records.map((row) => row[column]);
    return acc;
  }, {});
}

function createSchema(row) {
  return Object.keys(row).reduce((acc, element) => {
    acc[element] = typeof row[element];
    return acc;
  }, {});
}

module.exports = { getProjectedColumns, changeRecordDimensionToArray, createSchema };
