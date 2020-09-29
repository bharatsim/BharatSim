import { useEffect, useState } from 'react';
import useLoader from './useLoader';

function useFetch(api, { data, query, params } = {}) {
  const [responseData, setResponseData] = useState();

  const { loadingState, startLoader, stopLoaderAfterError, stopLoaderAfterSuccess } = useLoader();

  const dependencies = data ? [...data] : [];

  useEffect(() => {
    async function fetchApiData() {
      startLoader();
      try {
        const resData = await api({ data, query, params });
        setResponseData(resData);
        stopLoaderAfterSuccess();
      } catch (e) {
        stopLoaderAfterError();
      }
    }

    fetchApiData();
  }, dependencies);

  return { data: responseData, loadingState: loadingState.state };
}

export default useFetch;
