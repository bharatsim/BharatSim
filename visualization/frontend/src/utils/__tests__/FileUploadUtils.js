import { fileUploadedStatus, getStatusAndMessageFor } from '../fileUploadUtils';

describe('File Upload utils', () => {
  it('should provide status for file upload', () => {
    expect(fileUploadedStatus).toMatchInlineSnapshot(`
      Object {
        "ERROR": "error",
        "LOADING": "loading",
        "SUCCESS": "success",
      }
    `);
  });

  it('should provide status and message for file uploading', () => {
    const expected = {
      status: 'loading',
      message: 'uploading file.csv',
    };

    const actual = getStatusAndMessageFor('loading', 'file.csv');

    expect(actual).toEqual(expected);
  });

  it('should provide status and message for file uploading error', () => {
    const expected = {
      status: 'error',
      message: 'Error occurred while unloading file.csv',
    };

    const actual = getStatusAndMessageFor('error', 'file.csv');

    expect(actual).toEqual(expected);
  });

  it('should provide status and message for file uploading success', () => {
    const expected = {
      status: 'success',
      message: 'file.csv successfully uploaded',
    };

    const actual = getStatusAndMessageFor('success', 'file.csv');

    expect(actual).toEqual(expected);
  });
});
