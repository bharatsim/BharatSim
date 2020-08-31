import React, { useRef, useState } from 'react';
import { Box, Button } from '@material-ui/core';

import useForm from '../../hook/useForm';
import FileInput from '../../uiComponent/FileInput';
import useModal from '../../hook/useModal';
import DataTypeConfigModal from './DataTypeConfigModal';

import { api } from '../../utils/api';
import { validateFile } from '../../utils/validators';
import {
  fileUploadedStatus,
  getStatusAndMessageFor,
  parseCsv,
  resetFileInput,
} from '../../utils/fileUploadUtils';

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

  function onCancel() {
    closeModal();
    resetFileInput(ref.current);
  }

  async function upload(file, schema) {
    setFileUpload(getStatusAndMessageFor(fileUploadedStatus.LOADING, file.name));
    api
      .uploadFileAndSchema(file, schema)
      .then(() => {
        setFileUpload(getStatusAndMessageFor(fileUploadedStatus.SUCCESS, file.name));
      })
      .catch(() => {
        setFileUpload(getStatusAndMessageFor(fileUploadedStatus.ERROR, file.name));
      })
      .finally(() => {
        resetFields([FILE_INPUT_KEY]);
        resetFileInput(ref.current);
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
    setFileUpload({ status: null, message: '' });
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
          onCancel={onCancel}
        />
      )}
    </>
  );
}

export default FileUpload;
