import React from 'react';
import { loaderStates } from '../../hook/useLoader';
import Loader from './Loader';
import Error from './Error';

export default function LoaderOrError({ children, loadingState }) {
  if (loadingState === loaderStates.LOADING) {
    return <Loader />;
  }
  if (loadingState === loaderStates.ERROR) {
    return <Error />;
  }
  return children;
}
