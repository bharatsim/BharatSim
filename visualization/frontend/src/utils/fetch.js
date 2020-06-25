import {httpMethods} from "../constants/httpMethods";
import axios from "axios";

export default function fetch({url, method = httpMethods.GET, headers = undefined, data = undefined}) {
  return axios({url, method, headers, data,}).then((res) => res.data);
}

