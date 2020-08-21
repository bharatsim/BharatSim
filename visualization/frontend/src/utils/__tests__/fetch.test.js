/* eslint-disable import/first */
import axios from 'axios';

import { fetch, uploadFile } from '../fetch';

jest.mock('axios', () => ({
  __esModule: true,
  default: jest.fn(() => Promise.resolve({ data: 'Hello test' })),
}));

describe('Fetch util', () => {
  it('should return data from server for given url', () => {
    fetch({ url: 'test/api' });

    expect(axios).toHaveBeenCalledWith({
      data: undefined,
      headers: undefined,
      method: 'get',
      url: 'test/api',
    });
  });

  it('should return data from server for given url, data, method, header', () => {
    fetch({ url: 'test/api', data: 'data', method: 'post', headers: 'header' });

    expect(axios).toHaveBeenCalledWith({
      data: 'data',
      headers: 'header',
      method: 'post',
      url: 'test/api',
    });
  });

  it('should upload file for given url and file', () => {
    uploadFile({ url: 'test/api', file: { name: 'test.csv' } });

    expect(axios).toHaveBeenCalledWith({
      data: expect.any(FormData),
      headers: { 'content-type': 'multipart/form-data' },
      method: 'post',
      url: 'test/api',
    });
  });
});
