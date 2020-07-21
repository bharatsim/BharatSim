import React, { useRef, useState } from 'react';
import { Button, Typography, Box } from '@material-ui/core';

import { uploadFile } from '../../utils/fetch';
import { url } from '../../utils/url';

const VALID_FILE_TYPES = ['text/csv'];

const FileUpload = () => {
  const [file, setFile] = useState();
  const [error, setError] = useState('');
  const ref = useRef();

  async function upload() {
    await uploadFile({ url: url.DATA_SOURCES, file });
    setFile(null);
    ref.current.value = '';
  }

  function onFileInputChange(event) {
    const uploadedFile = event.target.files[0];
    if (uploadedFile && !VALID_FILE_TYPES.includes(uploadedFile.type)) {
      setError('Only csv files are allowed, Please upload csv');
      return;
    }
    setError('');
    setFile(uploadedFile);
  }

  return (
    <>
      <Box pb={2}>
        <input type="file" ref={ref} data-testid="input-upload-file" accept=".csv" onChange={onFileInputChange} />
        <Box>{error && <Typography color="error">{error}</Typography>}</Box>
      </Box>
      <Button
        type="button"
        onClick={upload}
        data-testid="button-upload"
        disabled={!!error || !file}
        variant="contained"
        color="primary"
      >
        Upload
      </Button>
    </>
  );
};

export default FileUpload;
