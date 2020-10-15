import useLoader, { loaderStates } from './useLoader';

function useFetchExecutor() {
  const { loadingState, startLoader, stopLoaderAfterError, stopLoaderAfterSuccess } = useLoader(
    loaderStates.SUCCESS,
  );

  function executeFetch(api, parameters = [], messages = {}) {
    startLoader(messages.loading);
    return api(...parameters)
      .then((data) => {
        stopLoaderAfterSuccess();
        return data;
      })
      .catch(() => {
        stopLoaderAfterError(messages.error);
      });
  }

  return { executeFetch, loadingState: loadingState.state };
}

export default useFetchExecutor;
