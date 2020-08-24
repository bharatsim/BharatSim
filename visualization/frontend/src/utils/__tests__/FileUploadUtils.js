import { fileUploadedStatus, getStatusAndMessageFor, parseCsv } from '../fileUploadUtils';

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
});
