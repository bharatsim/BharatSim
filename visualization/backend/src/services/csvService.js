const csvParser = require('papaparse');
const fs = require('fs');


function parseCSV() {
    const csvString = fs.readFileSync('./data/simulation.csv', 'utf-8');
    const csvData = csvParser.parse(csvString, {header: true, skipEmptyLines: true, dynamicTyping: true});
    return csvData;
}

function getData() {
    const csvData = parseCSV();
    const hour = csvData.data.map((row) => row.hour);
    const exposed = csvData.data.map((row) => row.exposed + row.hour);
    return {columns: {hour, exposed}}
}

module.exports = {
    getData
}
