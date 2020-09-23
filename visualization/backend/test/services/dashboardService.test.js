const { saveDashboard, getAllDashboards } = require('../../src/services/dashboardService');
const dashboardRepository = require('../../src/repository/dashboardRepository');
const InvalidInputException = require('../../src/exceptions/InvalidInputException');

jest.mock('../../src/repository/dashboardRepository');

const dashboardDataToUpdate = { dashboardId: 'id', dataConfig: { name: 'newName' } };
const dashboardDataToAdd = { dashboardId: undefined, dataConfig: { name: 'name' } };

describe('Dashboard Service', function () {
  it('should insert dashboard data if id is undefined ', function () {
    dashboardRepository.insert.mockResolvedValue({ _id: 'new_id' });

    saveDashboard(dashboardDataToAdd);

    expect(dashboardRepository.insert).toHaveBeenCalledWith({ dataConfig: { name: 'name' } });
  });
  it('should insert dashboard data if id is undefined and return new id', async function () {
    dashboardRepository.insert.mockResolvedValue({ _id: 'new_id' });

    const result = await saveDashboard(dashboardDataToAdd);

    expect(result).toEqual({ dashboardId: 'new_id' });
  });

  it('should update dashboard data for given id ', function () {
    saveDashboard(dashboardDataToUpdate);
    expect(dashboardRepository.update).toHaveBeenCalledWith('id', {
      dataConfig: { name: 'newName' },
    });
  });
  it('should throw error for invalid inputs while updating', async function () {
    dashboardRepository.update.mockImplementationOnce(() => {
      throw new Error('msg');
    });

    const result = async () => {
      await saveDashboard(dashboardDataToUpdate);
    };
    await expect(result).rejects.toThrow(
      new InvalidInputException('Error while updating dashboard'),
    );
  });
  it('should throw error for invalid inputs while inserting', async function () {
    dashboardRepository.insert.mockRejectedValue(new Error('msg'));

    const result = async () => {
      await saveDashboard(dashboardDataToAdd);
    };
    await expect(result).rejects.toThrow(
      new InvalidInputException('Error while inserting dashboard'),
    );
  });

  it('should update dashboard data and return new id', async function () {
    const result = await saveDashboard(dashboardDataToUpdate);

    expect(result).toEqual({ dashboardId: 'id' });
  });
  it('should get all dashboards', async function () {
    await getAllDashboards(dashboardDataToUpdate);
    expect(dashboardRepository.getAll).toHaveBeenCalled();
  });
});
