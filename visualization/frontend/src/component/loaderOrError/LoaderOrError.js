import React from 'react';
import { loaderStates } from '../../hook/useInlineLoader';
import Loader from './Loader';
import Error from './Error';

export default function LoaderOrError({ children, loadingState }) {
  if (loadingState === loaderStates.LOADING || loadingState === '') {
    return <Loader />;
  }
  if (loadingState === loaderStates.ERROR) {
    return <Error />;
  }
  return children;
}
