import React, { useRef, useState } from 'react';
import { Box, Button } from '@material-ui/core';

import useForm from '../../hook/useForm';
import { uploadFile } from '../../utils/fetch';
import { url } from '../../utils/url';
import { validateFile } from '../../utils/validators';
import { fileUploadedStatus, getStatusAndMessageFor } from '../../utils/fileUploadUtils';
import FileInput from '../../uiComponent/FileInput';

const FILE_INPUT_KEY = 'fileInput';

function FileUpload() {
  const [fileUpload, setFileUpload] = useState({ status: null, message: '' });
  const { errors, validateAndSetValue, resetFields, onSubmit, shouldEnableSubmit } = useForm({
    [FILE_INPUT_KEY]: validateFile,
  });
  const ref = useRef();

  const isUploadDisable = !shouldEnableSubmit() || fileUpload.status === fileUploadedStatus.LOADING;

  async function upload(file) {
    setFileUpload(getStatusAndMessageFor(fileUploadedStatus.LOADING, file.name));
    uploadFile({ url: url.DATA_SOURCES, file })
      .then(() => {
        setFileUpload(getStatusAndMessageFor(fileUploadedStatus.SUCCESS, file.name));
      })
      .catch(() => {
        setFileUpload(getStatusAndMessageFor(fileUploadedStatus.ERROR, file.name));
      })
      .finally(() => {
        resetFields([FILE_INPUT_KEY]);
        ref.current.value = '';
      });
  }

  async function validateAndUploadFile() {
    onSubmit((validatedValues) => upload(validatedValues[FILE_INPUT_KEY]));
  }

  function onFileInputChange(uploadedFile) {
    validateAndSetValue(FILE_INPUT_KEY, uploadedFile);
  }

  return (
    <>
      <Box pb={2}>
        <FileInput
          error={errors[FILE_INPUT_KEY]}
          onChange={onFileInputChange}
          fileUploadStatus={fileUpload.status}
          fileUploadStatusMessage={fileUpload.message}
          ref={ref}
        />
      </Box>
      <Button
        type="button"
        onClick={validateAndUploadFile}
        data-testid="button-upload"
        disabled={isUploadDisable}
        variant="contained"
        color="primary"
      >
        Upload
      </Button>
    </>
  );
}

export default FileUpload;
