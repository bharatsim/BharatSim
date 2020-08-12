import React, { useRef, useState } from 'react';
import { Button, Typography, Box } from '@material-ui/core';

import { uploadFile } from '../utils/fetch';
import { url } from '../utils/url';

const VALID_FILE_TYPES = ['text/csv'];
const MAX_FILE_SIZE = 10485760;

function isValidFile(file) {
  if (!VALID_FILE_TYPES.includes(file.type)) {
    return false;
  }
  if (file.size > MAX_FILE_SIZE) {
    return false;
  }
  return true;
}

const status = (fileName) => ({
  error: {
    msg: `Error occurred while unloading ${fileName}`,
    color: 'error',
    uploadButtonDisable: true,
  },
  success: {
    msg: `${fileName} successfully uploaded`,
    color: 'primary',
    uploadButtonDisable: false,
  },
  validationError: {
    msg: 'Only csv files are allowed of maxmimum size 10MB, Please upload valid csv a file',
    color: 'error',
    uploadButtonDisable: true,
  },
  loading: { msg: `uploading ${fileName}`, color: 'secondary', uploadButtonDisable: true },
});

const FileUpload = () => {
  const [file, setFile] = useState();
  const [fileUploadStatus, setFileUploadStatus] = useState(null);
  const ref = useRef();

  async function upload() {
    setFileUploadStatus(status(file.name).loading);
    uploadFile({ url: url.DATA_SOURCES, file })
      .then(() => {
        setFileUploadStatus(status(file.name).success);
      })
      .catch(() => {
        setFileUploadStatus(status(file.name).error);
      });
    setFile(null);
    ref.current.value = '';
  }

  function onFileInputChange(event) {
    const uploadedFile = event.target.files[0];
    if (uploadedFile && !isValidFile(uploadedFile)) {
      setFileUploadStatus(status(uploadedFile.name).validationError);
      return;
    }
    setFileUploadStatus(null);
    setFile(uploadedFile);
  }

  return (
    <>
      <Box pb={2}>
        <input
          type="file"
          ref={ref}
          data-testid="input-upload-file"
          accept=".csv"
          onChange={onFileInputChange}
        />
        <Box>
          {fileUploadStatus && (
            <Typography color={fileUploadStatus.color}>{fileUploadStatus.msg}</Typography>
          )}
        </Box>
      </Box>
      <Button
        type="button"
        onClick={upload}
        data-testid="button-upload"
        disabled={(fileUploadStatus && fileUploadStatus.uploadButtonDisable) || !file}
        variant="contained"
        color="primary"
      >
        Upload
      </Button>
    </>
  );
};

export default FileUpload;
