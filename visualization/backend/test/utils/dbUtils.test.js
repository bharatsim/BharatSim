const dbUtils = require('../../src/utils/dbUtils');

describe('dbUtils', () => {
  it('should change record dimension to array', async () => {
    const data = dbUtils.changeRecordDimensionToArray([{ hours: 1 }, { hours: 2 }, { hours: 3 }]);

    expect(data).toEqual({
      hours: [1, 2, 3],
    });
  });

  it('should get projected columns', async () => {
    const columnNames = dbUtils.getProjectedColumns(['hours', 'susceptible']);
    expect(columnNames).toEqual({
      hours: 1,
      susceptible: 1,
    });
  });

  it('should get excluded columns', async () => {
    const columnNames = dbUtils.getProjectedColumns(undefined);
    expect(columnNames).toEqual({
      __v: 0,
    });
  });

  it('should create schema from row of data', async () => {
    const schema = dbUtils.createSchema({ hour: 123, susceptible: 123 });

    expect(schema).toEqual({
      hour: 'number',
      susceptible: 'number',
    });
  });
});
