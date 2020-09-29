import axios from 'axios';
import { httpMethods } from '../constants/fetch';

function fetchData({ url, method = httpMethods.GET, headers, data, query }) {
  return axios({ url, method, headers, data, params: query })
    .then((res) => res.data)
    .catch((err) => {
      return err;
    });
}

function uploadData({ url, method = httpMethods.POST, headers, data, query }) {
  return fetchData({
    url,
    method,
    headers,
    data,
    params: query,
  });
}

export { fetchData, uploadData };
