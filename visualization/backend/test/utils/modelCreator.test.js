const mongoose = require('mongoose');
const { createModel } = require('../../src/utils/modelCreator');

jest.mock('mongoose');

describe('Model Creator', () => {
  it('should create data model for given skeleton', async () => {
    mongoose.model.mockImplementationOnce(() => {
      throw new Error('Model not found');
    });
    mongoose.model.mockReturnValueOnce('Model');

    const model = createModel('modelName', { column: 'string' });

    expect(mongoose.model).toHaveBeenCalledWith('modelName', expect.any(mongoose.Schema));
    expect(mongoose.Schema).toHaveBeenCalledWith({ column: 'string' }, { collection: 'modelName' });
    expect(model).toEqual('Model');
  });

  it('should provide already present data model', async () => {
    mongoose.model.mockReturnValueOnce('Model');

    const model = createModel('modelName', { column: 'string' });

    expect(mongoose.model).toHaveBeenCalledWith('modelName');
    expect(model).toEqual('Model');
  });
});
