const fs = require('fs');
const csvParser = require('papaparse');

const InvalidInputException = require('../exceptions/InvalidInputException');

function validateAndParseCSV(path) {
  const csvString = fs.readFileSync(path, 'utf-8');
  const { data, errors } = csvParser.parse(csvString, {
    header: true,
    skipEmptyLines: true,
    dynamicTyping: true,
  });
  if (errors.length > 0) {
    throw new InvalidInputException('Error while parsing csv');
  }
  return data;
}

module.exports = {
  validateAndParseCSV,
};
