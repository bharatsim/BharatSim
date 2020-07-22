import React, { useRef, useState } from 'react';
import { Button, Typography, Box } from '@material-ui/core';

import { uploadFile } from '../../utils/fetch';
import { url } from '../../utils/url';

const VALID_FILE_TYPES = ['text/csv'];

const status = {
  error: { msg: 'file not upload', color: 'error', uploadButtonDisable: true },
  success: { msg: 'file uploded', color: 'primary', uploadButtonDisable: false },
  validationError: { msg: 'Only csv files are allowed, Please upload csv', color: 'error', uploadButtonDisable: true },
  loading: { msg: 'uploading', color: 'secondary', uploadButtonDisable: true },
};

const FileUpload = () => {
  const [file, setFile] = useState();
  const [fileUploadStatus, setFileUploadStatus] = useState(null);
  const ref = useRef();

  async function upload() {
    setFileUploadStatus(status.loading);
    uploadFile({ url: url.DATA_SOURCES, file })
      .then(() => {
        setFileUploadStatus(status.success);
      })
      .catch(() => {
        setFileUploadStatus(status.error);
      });
    setFile(null);
    ref.current.value = '';
  }

  function onFileInputChange(event) {
    const uploadedFile = event.target.files[0];
    if (uploadedFile && !VALID_FILE_TYPES.includes(uploadedFile.type)) {
      setFileUploadStatus(status.validationError);
      return;
    }
    setFileUploadStatus(null);
    setFile(uploadedFile);
  }

  return (
    <>
      <Box pb={2}>
        <input type="file" ref={ref} data-testid="input-upload-file" accept=".csv" onChange={onFileInputChange} />
        <Box>{fileUploadStatus && <Typography color={fileUploadStatus.color}>{fileUploadStatus.msg}</Typography>}</Box>
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
