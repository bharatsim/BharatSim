import React from 'react';
import PropTypes from 'prop-types';
import { loaderStates } from '../../hook/useLoader';
import Loader from './Loader';
import Error from './Error';
import { ChildrenPropTypes } from '../../commanPropTypes';

export default function LoaderOrError({ children, loadingState, snackbar }) {
  if (snackbar && (loadingState === loaderStates.ERROR || loadingState === loaderStates.SUCCESS)) {
    return children;
  }
  if (loadingState === loaderStates.SUCCESS) {
    return children;
  }
  if (loadingState === loaderStates.ERROR) {
    return <Error />;
  }
  return <Loader />;
}

LoaderOrError.defaultProps = {
  snackbar: false,
}

LoaderOrError.propTypes = {
  children: ChildrenPropTypes.isRequired,
  loadingState: PropTypes.oneOf(['', ...Object.values(loaderStates)]).isRequired,
  snackbar: PropTypes.bool
};
