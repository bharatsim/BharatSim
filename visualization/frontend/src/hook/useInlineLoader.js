import { useState } from 'react';

export const loaderStates = {
  LOADING: 'LOADING',
  SUCCESS: 'SUCCESS',
  ERROR: 'ERROR',
};

function useInlineLoader() {
  const [loadingState, setLoadingState] = useState({ state: '', message: '' });
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
    setLoadingState({ state: '', message: '' });
  }
  return { loadingState, startLoader, stopLoaderAfterSuccess, stopLoaderAfterError, resetLoader };
}

export default useInlineLoader;
