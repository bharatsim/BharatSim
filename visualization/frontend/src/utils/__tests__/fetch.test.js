/* eslint-disable import/first */
import axios from 'axios';

import { fetchData, formDataBuilder, headerBuilder, uploadData } from '../fetch';

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

  it('should provide form data object with provide data array of name and value', () => {
    const actual = formDataBuilder([
      { name: 'test', value: 'test' },
      { name: 'test1', value: 'test1' },
    ]);

    expect(actual instanceof FormData).toBeTruthy();
    expect(actual.get('test')).toEqual('test');
    expect(actual.get('test1')).toEqual('test1');
  });

  it('should provide header with provided content type', () => {
    const actual = headerBuilder({ contentType: 'file' });

    expect(actual).toEqual({ 'content-type': 'file' });
  });
});
