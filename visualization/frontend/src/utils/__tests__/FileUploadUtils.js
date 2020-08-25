import {
  fileUploadedStatus,
  getFileUploadData,
  getFileUploadHeader,
  getStatusAndMessageFor,
  parseCsv,
} from '../fileUploadUtils';

jest.mock('papaparse', () => {
  return {
    parse: (csvFile, config) => {
      config.complete('data');
    },
  };
});

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

  it('should parse the csv file and call the given function', () => {
    const onApply = jest.fn();

    parseCsv('MockFile', onApply);

    expect(onApply).toHaveBeenCalledWith('data');
  });

  it('should provide file upload header', () => {
    const headers = getFileUploadHeader();

    expect(headers).toEqual({ 'content-type': 'multipart/form-data' });
  });

  it('should FormData object for file upload data', () => {
    const headers = getFileUploadData('file', 'schema');

    expect(headers).toBeInstanceOf(FormData);
  });
});
