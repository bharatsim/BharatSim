import { renderHook } from '@testing-library/react-hooks';

import useFetch from '../useFetch';

describe('Use fetch hook', () => {
  let api;
  beforeEach(() => {
    api = jest.fn().mockImplementation(async () => Promise.resolve('Hello NewUserHomeScreen'));
  });
  afterEach(() => {
    jest.clearAllMocks();
  });

  it('should return fetch data for given url', async () => {
    const { result, waitForNextUpdate } = renderHook(() => useFetch(api));

    await waitForNextUpdate();

    expect(result.current.data).toEqual('Hello NewUserHomeScreen');
    expect(api).toHaveBeenCalledWith({ data: undefined, params: undefined, query: undefined });
  });

  it('should return fetch data for given url and other data ', async () => {
    const { result, waitForNextUpdate } = renderHook(() =>
      useFetch(api, {
        data: 'data',
        params: 'params',
        query: 'query',
      }),
    );

    await waitForNextUpdate();

    expect(result.current.data).toEqual('Hello NewUserHomeScreen');
    expect(api).toHaveBeenCalledWith({
      data: 'data',
      params: 'params',
      query: 'query',
    });
  });

  it('should return success for loading state if data fetch is successful', async () => {
    const { result, waitForNextUpdate } = renderHook(() => useFetch(api));

    await waitForNextUpdate();

    expect(result.current.loadingState).toEqual('SUCCESS');
  });

  it('should return loading for loading state if data fetch is unsuccessful', async () => {
    api = jest.fn().mockRejectedValue('error');
    const { result, waitForNextUpdate } = renderHook(() => useFetch(api));

    await waitForNextUpdate();

    expect(result.current.loadingState).toEqual('ERROR');
  });

  it('should return error for loading state while fetching data', async () => {
    const { result, waitForNextUpdate } = renderHook(() => useFetch(api));

    expect(result.current.loadingState).toEqual('LOADING');

    await waitForNextUpdate();
  });
});
