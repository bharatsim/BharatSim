import axios from 'axios';
import fetch from "../fetch";

jest.mock('axios', () => ({
  __esModule: true,
  default: jest.fn(() => Promise.resolve({data: "Hello test"})),
}));

describe('Fetch util', function () {
  it('should return data from server for given url', function () {
    fetch({url: 'test/api'});

    expect(axios).toHaveBeenCalledWith({"data": undefined, "headers": undefined, "method": "get", "url": "test/api"});
  });

  it('should return data from server for given url, data, method, header', function () {
    fetch({url: 'test/api', data: 'data' , method: 'post', headers: 'header'});

    expect(axios).toHaveBeenCalledWith({"data": 'data', "headers": 'header', "method": "post", "url": "test/api"});
  });
});