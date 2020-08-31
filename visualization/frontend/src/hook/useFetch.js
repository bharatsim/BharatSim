import { useEffect, useState } from 'react';

function useFetch(api, { data, query, params } = {}) {
  const [responseData, setResponseData] = useState();

  useEffect(() => {
    async function fetchApiData() {
      const resData = await api({ data, query, params });
      setResponseData(resData);
    }

    fetchApiData();
  }, []);

  return responseData;
}

export default useFetch;
