import React from 'react';
import PropTypes from 'prop-types';

import { FormHelperText } from '@material-ui/core';

import { loaderStates } from '../../hook/useInlineLoader';

function InlineLoader({ status, message }) {
  return (
    <>
      {!!status && <FormHelperText error={status === loaderStates.ERROR}>{message}</FormHelperText>}
    </>
  );
}

InlineLoader.propTypes = {
  status: PropTypes.oneOf(['',...Object.values(loaderStates)]).isRequired,
  message: PropTypes.string.isRequired,
};

export default InlineLoader;
