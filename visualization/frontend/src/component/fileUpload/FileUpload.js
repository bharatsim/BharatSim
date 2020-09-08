import React, { useRef, useState } from 'react';
import { Box, Button } from '@material-ui/core';

import useForm from '../../hook/useForm';
import FileInput from '../../uiComponent/FileInput';
import useModal from '../../hook/useModal';
import DataTypeConfigModal from './DataTypeConfigModal';

import { api } from '../../utils/api';
import { validateFile } from '../../utils/validators';
import { getMessage, parseCsv, resetFileInput } from '../../utils/fileUploadUtils';
import useInlineLoader, { loaderStates } from '../../hook/useInlineLoader';
import InlineLoader from '../loaderOrError/InlineLoader';

const FILE_INPUT_KEY = 'fileInput';

function FileUpload() {
  const [parsedData, setParsedData] = useState(null);
  const { isOpen, closeModal, openModal } = useModal();
  const {
    loadingState,
    startLoader,
    stopLoaderAfterError,
    stopLoaderAfterSuccess,
    resetLoader,
  } = useInlineLoader();

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

  const isUploadDisable = !shouldEnableSubmit() || loadingState.state === loaderStates.LOADING;

  function onCancel() {
    closeModal();
    resetFileInput(ref.current);
  }

  async function upload(file, schema) {
    startLoader(getMessage(loaderStates.LOADING, file.name));
    api
      .uploadFileAndSchema(file, schema)
      .then(() => {
        stopLoaderAfterSuccess(getMessage(loaderStates.SUCCESS, file.name));
      })
      .catch(() => {
        stopLoaderAfterError(getMessage(loaderStates.ERROR, file.name));
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
    resetLoader();
    validateAndSetValue(FILE_INPUT_KEY, uploadedFile);
  }

  return (
    <>
      <Box pb={2}>
        <FileInput
          error={errors[FILE_INPUT_KEY]}
          onChange={onFileInputChange}
          fileUploadStatus={loadingState.state}
          fileUploadStatusMessage={loadingState.message}
          ref={ref}
        />
      </Box>
      <InlineLoader status={loadingState.state} message={loadingState.message} />
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
