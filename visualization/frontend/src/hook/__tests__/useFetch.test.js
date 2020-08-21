import { renderHook } from '@testing-library/react-hooks';

import useFetch from '../useFetch';
import { httpMethods } from '../../constants/fetch';
import { fetch } from '../../utils/fetch';

jest.mock('../../utils/fetch');

describe('Use fetch hook', () => {
  beforeEach(() => {
    fetch.mockReturnValue(Promise.resolve('Hello Welcome'));
  });

  it('should return fetch data for given url', async () => {
    const { result, waitForNextUpdate } = renderHook(() => useFetch({ url: '/test/api' }));

    await waitForNextUpdate();

    expect(result.current).toEqual('Hello Welcome');
    expect(fetch).toHaveBeenCalledWith({ method: 'get', url: '/test/api' });
  });

  it('should return fetch data for given url and other data ', async () => {
    const { result, waitForNextUpdate } = renderHook(() =>
      useFetch({
        url: '/test/api',
        headers: 'headers',
        method: httpMethods.POST,
        data: 'data',
        query: 'page',
      }),
    );

    await waitForNextUpdate();

    expect(result.current).toEqual('Hello Welcome');
    expect(fetch).toHaveBeenCalledWith({
      data: 'data',
      headers: 'headers',
      method: 'post',
      query: 'page',
      url: '/test/api',
    });
  });
});
