import {useEffect, useState} from "react";
import fetch from "../utils/fetch";
import {httpMethods} from "../constants/httpMethods";

const useFetch = (url, method=httpMethods.GET) => {
  const [data, setData] = useState();

  useEffect( () => {
    const fetchData = async () => {
      const resData = await fetch({url,method});
      setData(resData)
    }
    fetchData();
  },[])

  return data;
}

export default useFetch;