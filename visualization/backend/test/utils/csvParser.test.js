const fs = require('fs');

const { validateAndParseCSV } = require('../../src/utils/csvParser');
const InvalidInputException = require('../../src/exceptions/InvalidInputException');

jest.mock('fs');

describe('CSV parser', function () {
  it('should provide parsed csv into json', function () {
    fs.readFileSync.mockReturnValue(`hour,susceptible,exposed
1,9999,1
2,9999,1
3,9999,1`);

    const parsedData = validateAndParseCSV('/csv');

    expect(parsedData).toEqual([
      { exposed: 1, hour: 1, susceptible: 9999 },
      { exposed: 1, hour: 2, susceptible: 9999 },
      { exposed: 1, hour: 3, susceptible: 9999 },
    ]);
  });

  it('should throw error for invalid csv', function () {
    fs.readFileSync.mockReturnValue(`hour,susceptible,exposed
1,9999,1
2,9999
3,9999,1`);

    const result = () => validateAndParseCSV('/csv');

    expect(result).toThrow(new InvalidInputException('Error while parsing csv'));
  });
});
