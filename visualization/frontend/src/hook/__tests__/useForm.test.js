import { renderHook, act } from '@testing-library/react-hooks';

import useForm from '../useForm';

describe('Use form hook', () => {
  let renderedHook;
  const inputValidators = {
    age: (value = '') => (value > 80 ? 'too old to apply' : ''),
    name: (value = '') => (value.length > 10 ? 'too long name' : ''),
  };

  beforeEach(() => {
    const { result } = renderHook(() => useForm({}, inputValidators));
    renderedHook = result;
  });
  describe('handleInputChange', () => {
    it('should set value without any error if age less than 80', () => {
      act(() => renderedHook.current.handleInputChange('age', 79));

      expect(renderedHook.current.values).toEqual({
        age: 79,
      });

      expect(renderedHook.current.errors).toEqual({
        age: '',
      });
    });

    it('should set default values to values', () => {
      const { result } = renderHook(() => useForm());
      expect(result.current.values).toEqual({});
    });

    it('should set value along with given error if age greater than 80', () => {
      act(() => renderedHook.current.handleInputChange('age', 81));

      expect(renderedHook.current.values).toEqual({
        age: 81,
      });

      expect(renderedHook.current.errors).toEqual({
        age: 'too old to apply',
      });
    });
  });
  describe('shouldEnableSubmit', () => {
    it('should return true if no field has error', () => {
      act(() => {
        act(() => renderedHook.current.handleInputChange('age', 70));
      });
      act(() => {
        act(() => renderedHook.current.handleInputChange('name', 'someone'));
      });

      expect(renderedHook.current.shouldEnableSubmit()).toEqual(true);
    });

    it('should return false if any field has error', () => {
      act(() => {
        act(() => renderedHook.current.handleInputChange('age', 79));
      });
      act(() => {
        renderedHook.current.handleInputChange('name', 'vary very long name');
      });

      expect(renderedHook.current.shouldEnableSubmit()).toEqual(false);
    });
  });
});
