import React, { forwardRef } from 'react';
import PropTypes from 'prop-types';

import { Box, FormHelperText } from '@material-ui/core';

const FileInput = forwardRef(function FileInputWithRef({ onChange, error }, ref) {
  function onFileInputChange(event) {
    const uploadedFile = event.target.files[0];
    onChange(uploadedFile);
  }

  return (
    <>
      <input
        type="file"
        ref={ref}
        data-testid="input-upload-file"
        accept=".csv"
        onChange={onFileInputChange}
      />
      <Box>{!!error && <FormHelperText error>{error}</FormHelperText>}</Box>
    </>
  );
});

FileInput.defaultProps = {
  error: '',
};

FileInput.propTypes = {
  onChange: PropTypes.func.isRequired,
  error: PropTypes.string,
};

export default FileInput;
