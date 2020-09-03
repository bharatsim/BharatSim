import axios from 'axios';
import { httpMethods } from '../constants/fetch';

let shouldShowError;

function commonErrorHandler(err) {
  shouldShowError(true);
}

function initApiConfig({ setError }) {
  shouldShowError = setError;
}

function fetchData({ url, method = httpMethods.GET, headers, data, query, customErrorHandler }) {
  return axios({ url, method, headers, data, params: query })
    .then((res) => res.data)
    .catch((err) => {
      if (customErrorHandler) {
        customErrorHandler(err);
        return;
      }
      commonErrorHandler(err);
    });
}

function uploadData({ url, headers, data, query, customErrorHandler }) {
  return fetchData({
    url,
    method: httpMethods.POST,
    headers,
    data,
    params: query,
    customErrorHandler,
  });
}

export { fetchData, uploadData, initApiConfig };
