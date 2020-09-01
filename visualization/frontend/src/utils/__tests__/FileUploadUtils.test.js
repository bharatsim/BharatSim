import { getMessage, parseCsv, resetFileInput } from '../fileUploadUtils';

jest.mock('papaparse', () => ({
  parse: (csvFile, config) => {
    config.complete('data');
  },
}));

describe('File Upload utils', () => {
  it('should provide status and message for file uploading', () => {
    const expected = 'uploading file.csv';

    const actual = getMessage('LOADING', 'file.csv');

    expect(actual).toEqual(expected);
  });

  it('should provide status and message for file uploading error', () => {
    const expected = 'Error occurred while unloading file.csv';

    const actual = getMessage('ERROR', 'file.csv');

    expect(actual).toEqual(expected);
  });

  it('should provide status and message for file uploading success', () => {
    const expected = 'file.csv successfully uploaded';

    const actual = getMessage('SUCCESS', 'file.csv');

    expect(actual).toEqual(expected);
  });

  it('should parse the csv file and call the given function', () => {
    const onApply = jest.fn();

    parseCsv('MockFile', onApply);

    expect(onApply).toHaveBeenCalledWith('data');
  });

  it('should reset file input', () => {
    const fileInput = {
      value: 'file',
      files: [],
    };

    resetFileInput(fileInput);

    expect(fileInput).toEqual({
      value: '',
      files: null,
    });
  });
});
