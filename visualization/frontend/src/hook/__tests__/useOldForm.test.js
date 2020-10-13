import { renderHook, act } from '@testing-library/react-hooks';

import useOldForm from '../useOldForm';

describe('Use form hook', () => {
  let renderedHook;
  const fieldValidators = {
    'address-input': (value = '') => {
      if (value.length < 5) return 'too small';
      return '';
    },
    'name-input': (value = '') => (value.length < 10 ? '' : 'too long'),
  };

  beforeEach(() => {
    const { result } = renderHook(() => useOldForm(fieldValidators));
    renderedHook = result;
  });

  describe('ValidateAndSetValue', () => {
    it('should validate and set the value when value is valid', () => {
      act(() =>
        renderedHook.current.validateAndSetValue('address-input', 'F-123, test road, test city.'),
      );

      expect(renderedHook.current.values).toEqual({
        'address-input': 'F-123, test road, test city.',
      });

      expect(renderedHook.current.errors).toEqual({
        'address-input': '',
      });
    });

    it('should validate and set the value when value is invalid', () => {
      act(() => renderedHook.current.validateAndSetValue('address-input', 'A'));

      expect(renderedHook.current.values).toEqual({
        'address-input': 'A',
      });

      expect(renderedHook.current.errors).toEqual({
        'address-input': 'too small',
      });
    });
  });

  describe('setError', () => {
    it('should set error value for given key', () => {
      act(() => {
        renderedHook.current.setError('email-input', 'email is already exit');
      });

      expect(renderedHook.current.errors).toEqual({
        'email-input': 'email is already exit',
      });
    });
  });

  describe('shouldEnableSubmit', () => {
    it('should return true if no field has error', () => {
      act(() => {
        renderedHook.current.validateAndSetValue('address-input', 'F-123, test road, test city.');
      });
      act(() => {
        renderedHook.current.validateAndSetValue('name-input', 'test name');
      });

      expect(renderedHook.current.shouldEnableSubmit()).toEqual(true);
    });

    it('should return false if any field has error', () => {
      act(() => {
        renderedHook.current.validateAndSetValue('address-input', 'F-123, test road, test city.');
      });
      act(() => {
        renderedHook.current.validateAndSetValue('name-input', 'vary very long name');
      });

      expect(renderedHook.current.shouldEnableSubmit()).toEqual(false);
    });
  });

  describe('onSubmit', () => {
    it('should run all validation on submit', () => {
      const submitCallback = jest.fn();
      act(() => {
        renderedHook.current.validateAndSetValue('address-input', 'F-12');
      });
      act(() => {
        renderedHook.current.validateAndSetValue('name-input', 'asdasdasdasdasdasdasdas');
      });

      act(() => {
        renderedHook.current.onSubmit(submitCallback);
      });

      expect(renderedHook.current.errors).toEqual({
        'address-input': 'too small',
        'name-input': 'too long',
      });
    });

    it('should not call on submit callback is any validation fail', () => {
      const submitCallback = jest.fn();
      act(() => {
        renderedHook.current.validateAndSetValue('address-input', 'F-1212331');
      });
      act(() => {
        renderedHook.current.validateAndSetValue('name-input', 'asdasdasdasdasdasdasdas');
      });

      act(() => {
        renderedHook.current.onSubmit(submitCallback);
      });

      expect(submitCallback).not.toHaveBeenCalled();
    });

    it('should call on submit callback with values if all validation pass', () => {
      const submitCallback = jest.fn();
      act(() => {
        renderedHook.current.validateAndSetValue('address-input', 'F-1212331');
      });
      act(() => {
        renderedHook.current.validateAndSetValue('name-input', 'asdasdasd');
      });

      act(() => {
        renderedHook.current.onSubmit(submitCallback);
      });

      expect(submitCallback).toHaveBeenCalledWith({
        'address-input': 'F-1212331',
        'name-input': 'asdasdasd',
      });
    });

    it('should reset fields', () => {
      act(() => {
        renderedHook.current.validateAndSetValue('address-input', 'F-1212331');
      });
      act(() => {
        renderedHook.current.validateAndSetValue('name-input', 'asdasdasd');
      });

      act(() => {
        renderedHook.current.resetFields(['address-input', 'name-input']);
      });

      expect(renderedHook.current.values).toEqual({
        'address-input': undefined,
        'name-input': undefined,
      });
    });
  });
});
