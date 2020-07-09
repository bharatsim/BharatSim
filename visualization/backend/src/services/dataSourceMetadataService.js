const csvParser = require('papaparse');
const fs = require('fs');

const dataSourceMetadataRepository = require('../repository/dataSourceMetadataRepository');

function parseCSV() {
  const csvString = fs.readFileSync('./data/simulation.csv', 'utf-8');
  return csvParser.parse(csvString, { header: true, skipEmptyLines: true, dynamicTyping: true });
}

function getData(selectedColumns) {
  const csvData = parseCSV();
  const columnsToReturn = selectedColumns || Object.keys(csvData.data[0]);
  return columnsToReturn.reduce(
    (acc, column) => {
      acc.columns[column] = csvData.data.map((row) => row[column]);
      return acc;
    },
    { columns: {} },
  );
}

async function getHeaders(dataSourceName) {
  const dataSource = await dataSourceMetadataRepository.getDataSourceSchema(dataSourceName);
  const headers = Object.keys(dataSource.dataSourceSchema);
  return { headers };
}

async function getDataSources() {
  const dataSources = await dataSourceMetadataRepository.getDataSourceNames();
  const dataSourceNames = dataSources.map((dataSource) => dataSource.name);
  return { dataSources: dataSourceNames };
}

module.exports = {
  getData,
  getHeaders,
  getDataSources,
};
