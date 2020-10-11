import { useEffect } from 'react';
import { renderHook } from '@testing-library/react-hooks';
import useDeepCompareMemoize from '../useDeepCompareMemoize';

describe('useDeepCompareMemoize hook', () => {
  it('should call callback for first time', () => {
    const dependencies = [{ value: 123 }];
    const callback = jest.fn();
    renderHook(() => useEffect(callback, useDeepCompareMemoize(dependencies)));

    expect(callback).toHaveBeenCalled();
  });

  it('should not call callback again same for rerender if dependencies are same', () => {
    const dependencies = [{ value: 123 }];
    const callback = jest.fn();
    const { rerender } = renderHook(() => useEffect(callback, useDeepCompareMemoize(dependencies)));

    rerender();

    expect(callback).toHaveBeenCalledTimes(1);
  });

  it('should call callback again for rerender if dependencies are different', () => {
    let dependencies = [{ value: 123, a: [{ b: 1 }] }];
    const callback = jest.fn();
    const { rerender } = renderHook(() => useEffect(callback, useDeepCompareMemoize(dependencies)));

    dependencies = [{ value: 123, a: [{ b: 2 }] }];
    rerender();

    expect(callback).toHaveBeenCalledTimes(2);
  });
});
