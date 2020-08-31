import axios from 'axios';
import { httpMethods } from '../constants/fetch';

function fetchData({ url, method = httpMethods.GET, headers, data, query }) {
  return axios({ url, method, headers, data, params: query }).then((res) => res.data);
}

function uploadData({ url, headers, data, query }) {
  return axios({ url, method: httpMethods.POST, headers, data, params: query }).then(
    (res) => res.data,
  );
}

export { fetchData, uploadData };
