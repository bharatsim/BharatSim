import React from 'react';
import PropTypes from 'prop-types';
import { loaderStates } from '../../hook/useLoader';
import Loader from './Loader';
import Error from './Error';
import { ChildrenPropTypes } from '../../commanPropTypes';

export default function LoaderOrError({ children, loadingState }) {
  if (loadingState === loaderStates.SUCCESS) {
    return children;
  }
  if (loadingState === loaderStates.ERROR) {
    return <Error />;
  }
  return <Loader />;
}

LoaderOrError.propTypes = {
  children: ChildrenPropTypes.isRequired,
  loadingState: PropTypes.oneOf(['', ...Object.values(loaderStates)]).isRequired,
};
