import { convertObjectArrayToOptions, convertStringArrayToOptions, updateState } from '../helper';

describe('Helpers', () => {
  describe('Update state', () => {
    it('should only update state data which is modified', () => {
      const prevState = { a: '1', b: '2' };
      const updatedData = { b: '3' };
      const expectedState = { a: '1', b: '3' };

      const updatedState = updateState(prevState, updatedData);

      expect(updatedState).toEqual(expectedState);
    });
  });

  describe('convertObjectArrayToOptions', () => {
    it('should provide option in value and display name format from array of object', () => {
      const arrayObject = [
        { a: '1', b: '2' },
        { a: '2', b: '3' },
        { a: '3', b: '4' },
      ];
      const expectedOptions = [
        { value: '1', displayName: '2' },
        { value: '2', displayName: '3' },
        { value: '3', displayName: '4' },
      ];

      const updatedState = convertObjectArrayToOptions(arrayObject, 'a', 'b');

      expect(updatedState).toEqual(expectedOptions);
    });
  });

  describe('convertStringArrayToOptions', () => {
    it('should provide option in value and display name format from array of strings', () => {
      const arrayObject = ['a', 'b', 'c'];
      const expectedOptions = [
        { value: 'a', displayName: 'a' },
        { value: 'b', displayName: 'b' },
        { value: 'c', displayName: 'c' },
      ];

      const updatedState = convertStringArrayToOptions(arrayObject);

      expect(updatedState).toEqual(expectedOptions);
    });
  });
});
