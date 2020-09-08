import { useEffect, useState } from 'react';
import useInlineLoader from './useInlineLoader';

function useFetch(api, { data, query, params } = {}) {
  const [responseData, setResponseData] = useState();

  const {
    loadingState,
    startLoader,
    stopLoaderAfterError,
    stopLoaderAfterSuccess,
  } = useInlineLoader();

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
