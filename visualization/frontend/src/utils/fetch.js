import {httpMethods} from "../constants/httpMethods";
import axios from "axios";

export default function fetch({url, method = httpMethods.GET, headers, data, query}) {
  return axios({url, method, headers, data, params: query}).then((res) => res.data);
}

