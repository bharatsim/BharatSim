import React, { forwardRef } from 'react';
import PropTypes from 'prop-types';

import { Box, FormHelperText } from '@material-ui/core';
import { fileUploadedStatus } from '../utils/fileUploadUtils';

const FileInput = forwardRef(function FileInputWithRef(
  { onChange, fileUploadStatus, fileUploadStatusMessage, error },
  ref,
) {
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
      <Box>
        {fileUploadStatus && (
          <FormHelperText error={fileUploadStatus === fileUploadedStatus.ERROR}>
            {fileUploadStatusMessage}
          </FormHelperText>
        )}
        {!!error && <FormHelperText error>{error}</FormHelperText>}
      </Box>
    </>
  );
});

FileInput.defaultProps = {
  fileUploadStatus: null,
  fileUploadStatusMessage: '',
  error: '',
};

FileInput.propTypes = {
  onChange: PropTypes.func.isRequired,
  fileUploadStatus: PropTypes.oneOf(Object.values(fileUploadedStatus)),
  fileUploadStatusMessage: PropTypes.string,
  error: PropTypes.string,
};

export default FileInput;
