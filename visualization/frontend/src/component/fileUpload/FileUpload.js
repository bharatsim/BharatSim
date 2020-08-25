import React, { useRef, useState } from 'react';
import { Box, Button } from '@material-ui/core';

import useForm from '../../hook/useForm';
import { uploadData } from '../../utils/fetch';
import { url } from '../../utils/url';
import { validateFile } from '../../utils/validators';
import {
  fileUploadedStatus,
  getFileUploadData,
  getFileUploadHeader,
  getStatusAndMessageFor,
  parseCsv,
} from '../../utils/fileUploadUtils';
import FileInput from '../../uiComponent/FileInput';
import useModal from '../../hook/useModal';
import DataTypeConfigModal from './DataTypeConfigModal';

const FILE_INPUT_KEY = 'fileInput';

function FileUpload() {
  const [fileUpload, setFileUpload] = useState({ status: null, message: '' });
  const [parsedData, setParsedData] = useState(null);
  const { isOpen, closeModal, openModal } = useModal();

  const {
    errors,
    validateAndSetValue,
    resetFields,
    onSubmit,
    shouldEnableSubmit,
    values,
  } = useForm({
    [FILE_INPUT_KEY]: validateFile,
  });
  const ref = useRef();

  const isUploadDisable = !shouldEnableSubmit() || fileUpload.status === fileUploadedStatus.LOADING;

  async function upload(file, schema) {
    setFileUpload(getStatusAndMessageFor(fileUploadedStatus.LOADING, file.name));
    uploadData({
      url: url.DATA_SOURCES,
      data: getFileUploadData(file, schema),
      headers: getFileUploadHeader(),
    })
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

  async function onSchemaChangesApply(selectedSchema) {
    await upload(values[FILE_INPUT_KEY], selectedSchema);
    closeModal();
  }

  function validateAndParseCsv() {
    onSubmit((validatedValues) => parseCsv(validatedValues[FILE_INPUT_KEY], onCompleteParseCsv));
  }

  function onCompleteParseCsv(parsedResult) {
    setParsedData(parsedResult.data);
    openModal();
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
        onClick={validateAndParseCsv}
        data-testid="button-upload"
        disabled={isUploadDisable}
        variant="contained"
        color="primary"
      >
        Upload
      </Button>
      {isOpen && !!parsedData && (
        <DataTypeConfigModal
          closeModal={closeModal}
          isOpen={isOpen}
          dataRow={parsedData[0]}
          onApply={onSchemaChangesApply}
        />
      )}
    </>
  );
}

export default FileUpload;
