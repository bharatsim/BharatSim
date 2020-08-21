import { useEffect, useState } from 'react';
import { fetch } from '../utils/fetch';
import { httpMethods } from '../constants/fetch';

function useFetch({ url, method = httpMethods.GET, headers, data, query }) {
  const [responseData, setResponseData] = useState();

  useEffect(() => {
    async function fetchData() {
      const resData = await fetch({ url, method, headers, data, query });
      setResponseData(resData);
    }

    fetchData();
  }, []);

  return responseData;
}

export default useFetch;
