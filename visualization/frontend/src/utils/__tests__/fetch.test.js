/* eslint-disable import/first */
import axios from 'axios';

import { fetchData, initApiConfig, uploadData } from '../fetch';

jest.mock('axios', () => ({
  __esModule: true,
  default: jest.fn(() => Promise.resolve({ data: 'Hello test' })),
}));

describe('Fetch util', () => {
  it('should return data from server for given url', () => {
    fetchData({ url: 'test/api' });

    expect(axios).toHaveBeenCalledWith({
      data: undefined,
      headers: undefined,
      method: 'get',
      url: 'test/api',
    });
  });

  it('should return data from server for given url, data, method, header', () => {
    fetchData({ url: 'test/api', data: 'data', method: 'post', headers: 'header' });

    expect(axios).toHaveBeenCalledWith({
      data: 'data',
      headers: 'header',
      method: 'post',
      url: 'test/api',
    });
  });

  it('should upload file for given url and file', () => {
    uploadData({ url: 'test/api', headers: 'headers', data: 'data' });

    expect(axios).toHaveBeenCalledWith({
      data: 'data',
      headers: 'headers',
      method: 'post',
      url: 'test/api',
    });
  });
  it('should call common error handler for api failure if handler is not provided', async () => {
    const commonErrorHandler = jest.fn();
    initApiConfig({ setError: commonErrorHandler });
    axios.mockRejectedValue('test');

    await fetchData({ url: 'test/api' });

    expect(commonErrorHandler).toHaveBeenCalled();
  });
  it('should call custom error handler for api failure', async () => {
    const customErrorHandler = jest.fn();
    axios.mockRejectedValue('test');
    await fetchData({ url: 'test/api', customErrorHandler });

    expect(customErrorHandler).toHaveBeenCalled();
  });
});
