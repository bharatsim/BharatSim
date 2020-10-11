import React from 'react';
import { render } from '@testing-library/react';
import { fireEvent } from '@testing-library/dom';
import withThemeProvider from '../../../theme/withThemeProvider';
import ImportDataset from '../ImportDataset';
import * as fileUtils from '../../../utils/fileUploadUtils';

jest.spyOn(fileUtils, 'parseCsv').mockImplementation((csvFile, onComplete) => {
  const data = { data: [{ col1: 'row1', col2: 1 }], errors: [] };
  onComplete(data);
});

describe('Upload Dataset', () => {
  const Component = withThemeProvider(ImportDataset);
  let setFileMock;
  let handleNextMock;
  let setPreviewDataMock;
  let setErrorStepMock;
  beforeEach(() => {
    setFileMock = jest.fn();
    handleNextMock = jest.fn();
    setPreviewDataMock = jest.fn();
    setErrorStepMock = jest.fn();
  });

  it('should match snapshot for import dataset component', () => {
    const { container } = render(
      <Component
        setFile={setFileMock}
        handleNext={handleNextMock}
        setPreviewData={setPreviewDataMock}
        setErrorStep={setErrorStepMock}
      />,
    );

    expect(container).toMatchSnapshot();
  });

  it('should import and set csv file ', () => {
    const { getByTestId } = render(
      <Component
        setFile={setFileMock}
        handleNext={handleNextMock}
        setPreviewData={setPreviewDataMock}
        setErrorStep={setErrorStepMock}
      />,
    );
    const inputComponent = getByTestId('file-input');

    fireEvent.change(inputComponent, { target: { files: [{ name: 'csv', size: '10' }] } });

    expect(setFileMock).toHaveBeenCalledWith({ name: 'csv', size: '10' });
  });
  it('should import and parse csv file ', () => {
    const { getByTestId } = render(
      <Component
        setFile={setFileMock}
        handleNext={handleNextMock}
        setPreviewData={setPreviewDataMock}
        setErrorStep={setErrorStepMock}
      />,
    );
    const inputComponent = getByTestId('file-input');

    fireEvent.change(inputComponent, { target: { files: [{ name: 'csv', size: '10' }] } });

    expect(fileUtils.parseCsv).toHaveBeenCalledWith(
      { name: 'csv', size: '10' },
      expect.any(Function, () => {}),
    );
  });
  it('should set preview data for given file', () => {
    const { getByTestId } = render(
      <Component
        setFile={setFileMock}
        handleNext={handleNextMock}
        setPreviewData={setPreviewDataMock}
        setErrorStep={setErrorStepMock}
      />,
    );
    const inputComponent = getByTestId('file-input');

    fireEvent.change(inputComponent, { target: { files: [{ name: 'csv', size: '10' }] } });

    expect(setPreviewDataMock).toHaveBeenCalledWith([{ col1: 'row1', col2: 1 }]);
  });
  it('should set error for any parsing error in given file', () => {
    jest.spyOn(fileUtils, 'parseCsv').mockImplementation((csvFile, onComplete) => {
      const data = { data: [{ col1: 'row1', col2: 1 }], errors: ['error'] };
      onComplete(data);
    });
    const { getByTestId } = render(
      <Component
        setFile={setFileMock}
        handleNext={handleNextMock}
        setPreviewData={setPreviewDataMock}
        setErrorStep={setErrorStepMock}
      />,
    );
    const inputComponent = getByTestId('file-input');

    fireEvent.change(inputComponent, { target: { files: [{ name: 'csv', size: '10' }] } });

    expect(setErrorStepMock).toHaveBeenCalledWith(0);
  });
});
