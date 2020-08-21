const fileUploadedStatus = {
  LOADING: 'loading',
  SUCCESS: 'success',
  ERROR: 'error',
};

function getStatusAndMessageFor(fileUploadStatus, fileName) {
  return {
    [fileUploadedStatus.ERROR]: {
      status: fileUploadedStatus.ERROR,
      message: `Error occurred while unloading ${fileName}`,
    },
    [fileUploadedStatus.SUCCESS]: {
      status: fileUploadedStatus.SUCCESS,
      message: `${fileName} successfully uploaded`,
    },
    [fileUploadedStatus.LOADING]: {
      status: fileUploadedStatus.LOADING,
      message: `uploading ${fileName}`,
    },
  }[fileUploadStatus];
}

export { fileUploadedStatus, getStatusAndMessageFor };
