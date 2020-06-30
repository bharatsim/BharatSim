import { updateState } from '../helper';

describe('Update state', () => {
  it('should only update state data which is modified', () => {
    const prevState = { a: '1', b: '2' };
    const updatedData = { b: '3' };
    const expectedState = { a: '1', b: '3' };

    const updatedState = updateState(prevState, updatedData);

    expect(updatedState).toEqual(expectedState);
  });
});
