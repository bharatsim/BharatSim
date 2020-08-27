const Dashboard = require('../../src/model/dashboard');
const DashboardRepository = require('../../src/repository/dashboardRepository');

const parseMongoDBResult = (result) => JSON.parse(JSON.stringify(result));

const dbHandler = require('../db-handler');

const widget = {
  config: { xAxis: 'xCol', yAxis: 'ycol' },
  dataSource: 'datasource',
  layout: { h: 1, i: 'test', w: 2, x: 1, y: null },
  chartType: 'chartType',
};

const dashboard = {
  name: 'dashboard1',
  widgets: [widget],
  layout: [],
};

describe('DashboardRepository', function () {
  beforeAll(async () => {
    await dbHandler.connect();
  });
  afterEach(async () => {
    await dbHandler.clearDatabase();
  });
  afterAll(async () => {
    await dbHandler.closeDatabase();
  });

  it('should insert dashboard data into database', async function () {
    const { _id } = await DashboardRepository.insert(dashboard);

    expect(parseMongoDBResult(await Dashboard.findOne({ _id }, { __v: 0, _id: 0 }))).toEqual({
      name: 'dashboard1',
      widgets: [
        {
          config: { xAxis: 'xCol', yAxis: 'ycol' },
          dataSource: 'datasource',
          layout: { h: 1, i: 'test', w: 2, x: 1, y: null },
          chartType: 'chartType',
        },
      ],
      layout: [],
    });
  });
  it('should update dashboard data into database', async function () {
    const { _id } = await DashboardRepository.insert(dashboard);
    const newData = { ...dashboard, name: 'newName' };

    await DashboardRepository.update(_id, newData);

    expect(parseMongoDBResult(await Dashboard.findOne({ _id }, { __v: 0, _id: 0 }))).toEqual({
      name: 'newName',
      widgets: [
        {
          config: { xAxis: 'xCol', yAxis: 'ycol' },
          dataSource: 'datasource',
          layout: { h: 1, i: 'test', w: 2, x: 1, y: null },
          chartType: 'chartType',
        },
      ],
      layout: [],
    });
  });
  it('should fetch all the uploaded dashboards from database', async function () {
    await DashboardRepository.insert(dashboard);
    await DashboardRepository.insert(dashboard);

    const data = parseMongoDBResult(await DashboardRepository.getAll());

    expect(data.length).toEqual(2);
  });
});
