const csvParser = require('papaparse');
const fs = require('fs');


function parseCSV() {
    const csvString = fs.readFileSync('./data/simulation.csv', 'utf-8');
    const csvData = csvParser.parse(csvString, {header: true, skipEmptyLines: true, dynamicTyping: true});
    return csvData;
}

function getData(selectedColumns) {
    const csvData = parseCSV();
    const columnsToReturn = selectedColumns  || getHeaders().headers;
    return columnsToReturn.reduce((acc, column)=>{
        acc.columns[column]  = csvData.data.map((row)=> row[column])
        return acc;
    },{columns: {}})

}

function getHeaders() {
    const csvData = parseCSV();
    return {headers: Object.keys(csvData.data[0])}
}

module.exports = {
    getData,
    getHeaders
}
