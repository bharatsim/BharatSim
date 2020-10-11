import { useEffect, useState } from 'react';

import useLoader from './useLoader';
import useDeepCompareMemoize from './useDeepCompareMemoize';

const defaultApiParameters = [];

function useFetch(api, apiParameters = defaultApiParameters) {
  const [responseData, setResponseData] = useState();

  const { loadingState, startLoader, stopLoaderAfterError, stopLoaderAfterSuccess } = useLoader();

  const shouldUpdate = useDeepCompareMemoize(apiParameters);

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
  }, [shouldUpdate]);

  return { data: responseData, loadingState: loadingState.state };
}

export default useFetch;
