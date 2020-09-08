import { renderHook, act } from '@testing-library/react-hooks';
import useInlineLoader from '../useInlineLoader';

describe('hook useInlineLoader', () => {
  it('should return empty state and message as initial state of loaderOrError', () => {
    const { result } = renderHook(() => useInlineLoader());
    expect(result.current.loadingState).toEqual({ state: '', message: '' });
  });

  it('should return loading state and message on startLoader', () => {
    const { result } = renderHook(() => useInlineLoader());
    act(() => {
      result.current.startLoader('loading');
    });

    expect(result.current.loadingState).toEqual({ state: 'LOADING', message: 'loading' });
  });

  it('should stopLoaderAfterSuccess', () => {
    const { result } = renderHook(() => useInlineLoader());
    act(() => {
      result.current.stopLoaderAfterSuccess('success');
    });

    expect(result.current.loadingState).toEqual({ state: 'SUCCESS', message: 'success' });
  });

  it('should stopLoaderAfterError', () => {
    const { result } = renderHook(() => useInlineLoader());
    act(() => {
      result.current.stopLoaderAfterError('error');
    });

    expect(result.current.loadingState).toEqual({ state: 'ERROR', message: 'error' });
  });

  it('should resetLoader', () => {
    const { result } = renderHook(() => useInlineLoader());
    act(() => {
      result.current.stopLoaderAfterSuccess('success');
    });
    expect(result.current.loadingState).toEqual({ state: 'SUCCESS', message: 'success' });
    act(() => {
      result.current.resetLoader();
    });
    expect(result.current.loadingState).toEqual({ state: '', message: '' });
  });
});
