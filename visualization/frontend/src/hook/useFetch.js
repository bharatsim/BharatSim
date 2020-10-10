import { useEffect, useState } from 'react';
import useLoader from './useLoader';

function useFetch(api, apiParameters = []) {
  const [responseData, setResponseData] = useState();

  const { loadingState, startLoader, stopLoaderAfterError, stopLoaderAfterSuccess } = useLoader();

  useEffect(() => {
    startLoader();

    async function fetchApiData() {
      try {
        const resData = await api(...apiParameters);
        setResponseData(resData);
        stopLoaderAfterSuccess();
      } catch (e) {
        stopLoaderAfterError();
      }
    }

    fetchApiData();
  }, apiParameters);

  return { data: responseData, loadingState: loadingState.state };
}

export default useFetch;
