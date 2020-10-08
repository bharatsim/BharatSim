import { useState } from 'react';

export const loaderStates = {
  LOADING: 'LOADING',
  SUCCESS: 'SUCCESS',
  ERROR: 'ERROR',
};

function useLoader(defultLoadingState = '') {
  const [loadingState, setLoadingState] = useState({ state: defultLoadingState, message: '' });

  function startLoader(message) {
    setLoadingState({ state: loaderStates.LOADING, message });
  }

  function stopLoaderAfterSuccess(message) {
    setLoadingState({ state: loaderStates.SUCCESS, message });
  }

  function stopLoaderAfterError(message) {
    setLoadingState({ state: loaderStates.ERROR, message });
  }

  function resetLoader() {
    setLoadingState({ state: defultLoadingState, message: '' });
  }

  return { loadingState, startLoader, stopLoaderAfterSuccess, stopLoaderAfterError, resetLoader };
}

export default useLoader;
