import { useEffect, useState } from 'react';
import { fetchData } from '../utils/fetch';
import { httpMethods } from '../constants/fetch';

function useFetch({ url, method = httpMethods.GET, headers, data, query }) {
  const [responseData, setResponseData] = useState();

  useEffect(() => {
    async function fetchApiData() {
      const resData = await fetchData({ url, method, headers, data, query });
      setResponseData(resData);
    }

    fetchApiData();
  }, []);

  return responseData;
}

export default useFetch;
