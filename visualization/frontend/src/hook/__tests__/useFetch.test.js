import { renderHook } from '@testing-library/react-hooks';

import useFetch from '../useFetch';

describe('Use fetch hook', () => {
  let api;
  beforeEach(() => {
    api = jest.fn().mockImplementation(async () => Promise.resolve('Hello Welcome'));
  });
  afterEach(() => {
    jest.clearAllMocks();
  });

  it('should return fetch data for given url', async () => {
    const { result, waitForNextUpdate } = renderHook(() => useFetch(api));

    await waitForNextUpdate();

    expect(result.current).toEqual('Hello Welcome');
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

    expect(result.current).toEqual('Hello Welcome');
    expect(api).toHaveBeenCalledWith({
      data: 'data',
      params: 'params',
      query: 'query',
    });
  });
});
