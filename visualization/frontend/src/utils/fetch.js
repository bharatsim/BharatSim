import axios from 'axios';
import { contentType, httpMethods } from '../constants/fetch';

function fetch({ url, method = httpMethods.GET, headers, data, query }) {
  return axios({ url, method, headers, data, params: query }).then((res) => res.data);
}

function uploadFile({ url, file }) {
  const headers = {
    'content-type': contentType.FILE,
  };
  const formData = new FormData();
  formData.append('datafile', file);
  return fetch({ url, method: httpMethods.POST, headers, data: formData });
}

export { fetch, uploadFile };
