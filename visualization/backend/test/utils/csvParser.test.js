const fs = require('fs');

const { parseCSV } = require('../../src/utils/csvParser');

jest.mock('fs');

describe('CSV parser', function () {
  it('should provide parsed csv into json', function () {
    fs.readFileSync.mockReturnValue(`hour,susceptible,exposed
1,9999,1
2,9999,1
3,9999,1`);

    const parsedData = parseCSV('/csv');

    expect(parsedData.data).toEqual([
      { exposed: 1, hour: 1, susceptible: 9999 },
      { exposed: 1, hour: 2, susceptible: 9999 },
      { exposed: 1, hour: 3, susceptible: 9999 },
    ]);
  });
});
