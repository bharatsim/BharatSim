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

function changeRecordDimensionToArray(records) {
  const columns = Object.keys(records[0]);
  return columns.reduce((acc, column) => {
    acc[column] = records.map((row) => row[column]);
    return acc;
  }, {});
}

module.exports = { getProjectedColumns, changeRecordDimensionToArray };
