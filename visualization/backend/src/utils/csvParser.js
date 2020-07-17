const fs = require('fs');
const csvParser = require('papaparse');

function parseCSV(path) {
  const csvString = fs.readFileSync(path, 'utf-8');
  return csvParser.parse(csvString, { header: true, skipEmptyLines: true, dynamicTyping: true });
}

module.exports = {
  parseCSV,
};
