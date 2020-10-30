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

describe('Import Dataset', () => {
  const Component = withThemeProvider(ImportDataset);
  let setFileMock;
  let handleNextMock;
  let setPreviewDataMock;
  let setErrorStepMock;
  let setSchemaMock;
  beforeEach(() => {
    setFileMock = jest.fn();
    handleNextMock = jest.fn();
    setPreviewDataMock = jest.fn();
    setErrorStepMock = jest.fn();
    setSchemaMock = jest.fn();
  });

  it('should match snapshot for import dataset component', () => {
    const { container } = render(
      <Component
        setFile={setFileMock}
        handleNext={handleNextMock}
        setPreviewData={setPreviewDataMock}
        setErrorStep={setErrorStepMock}
        setSchema={setSchemaMock}
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
        setSchema={setSchemaMock}
      />,
    );
    const inputComponent = getByTestId('file-input');

    fireEvent.change(inputComponent, {
      target: { files: [{ name: 'csv', size: '10', type: 'text/csv' }] },
    });

    expect(setFileMock).toHaveBeenCalledWith({ name: 'csv', size: '10', type: 'text/csv' });
  });
  it('should import and parse csv file ', () => {
    const { getByTestId } = render(
      <Component
        setFile={setFileMock}
        handleNext={handleNextMock}
        setPreviewData={setPreviewDataMock}
        setErrorStep={setErrorStepMock}
        setSchema={setSchemaMock}
      />,
    );
    const inputComponent = getByTestId('file-input');

    fireEvent.change(inputComponent, {
      target: { files: [{ name: 'csv', size: '10', type: 'text/csv' }] },
    });

    expect(fileUtils.parseCsv).toHaveBeenCalledWith(
      { name: 'csv', size: '10', type: 'text/csv' },
      expect.any(Function, () => {}),
    );
  });

  it('should create and set schema', () => {
    const { getByTestId } = render(
      <Component
        setFile={setFileMock}
        handleNext={handleNextMock}
        setPreviewData={setPreviewDataMock}
        setErrorStep={setErrorStepMock}
        setSchema={setSchemaMock}
      />,
    );
    const inputComponent = getByTestId('file-input');

    fireEvent.change(inputComponent, {
      target: { files: [{ name: 'csv', size: '10', type: 'text/csv' }] },
    });

    expect(setSchemaMock).toHaveBeenCalledWith({
      col1: 'String',
      col2: 'Number',
    });
  });
  it('should set preview data for given file', () => {
    const { getByTestId } = render(
      <Component
        setFile={setFileMock}
        handleNext={handleNextMock}
        setPreviewData={setPreviewDataMock}
        setErrorStep={setErrorStepMock}
        setSchema={setSchemaMock}
      />,
    );
    const inputComponent = getByTestId('file-input');

    fireEvent.change(inputComponent, {
      target: { files: [{ name: 'csv', size: '10', type: 'text/csv' }] },
    });

    expect(setPreviewDataMock).toHaveBeenCalledWith([{ col1: 'row1', col2: 1 }]);
  });
  it('should set error for any parsing error in given file', () => {
    jest.spyOn(fileUtils, 'parseCsv').mockImplementation((csvFile, onComplete) => {
      const data = { data: [{ col1: 'row1', col2: 1 }], errors: ['error'] };
      onComplete(data);
    });
    const { getByTestId, queryByText } = render(
      <Component
        setFile={setFileMock}
        handleNext={handleNextMock}
        setPreviewData={setPreviewDataMock}
        setErrorStep={setErrorStepMock}
        setSchema={setSchemaMock}
      />,
    );
    const inputComponent = getByTestId('file-input');

    fireEvent.change(inputComponent, {
      target: { files: [{ name: 'csv', size: '10', type: 'text/csv' }] },
    });

    expect(
      queryByText(
        'Failed to Import file due to parsing error. Please review the file and ensure that its a valid CSV file.',
      ),
    ).toBeInTheDocument();
    expect(setErrorStepMock).toHaveBeenCalledWith(0);
  });
  it('should set validation error for given file if size exceed limit of 10mb', () => {
    jest.spyOn(fileUtils, 'parseCsv').mockImplementation((csvFile, onComplete) => {
      const data = { data: [{ col1: 'row1', col2: 1 }], errors: [] };
      onComplete(data);
    });
    const { getByTestId, queryByText } = render(
      <Component
        setFile={setFileMock}
        handleNext={handleNextMock}
        setPreviewData={setPreviewDataMock}
        setErrorStep={setErrorStepMock}
        setSchema={setSchemaMock}
      />,
    );
    const inputComponent = getByTestId('file-input');

    fireEvent.change(inputComponent, {
      target: { files: [{ name: 'csv', size: '120843092842123', type: 'text/csv' }] },
    });

    expect(
      queryByText('Failed to Import file, size exceeds the limit of 10MB'),
    ).toBeInTheDocument();
    expect(setErrorStepMock).toHaveBeenCalledWith(0);
  });
  it('should set validation error for given file if type if not supported', () => {
    jest.spyOn(fileUtils, 'parseCsv').mockImplementation((csvFile, onComplete) => {
      const data = { data: [{ col1: 'row1', col2: 1 }], errors: [] };
      onComplete(data);
    });
    const { getByTestId, queryByText } = render(
      <Component
        setFile={setFileMock}
        handleNext={handleNextMock}
        setPreviewData={setPreviewDataMock}
        setErrorStep={setErrorStepMock}
        setSchema={setSchemaMock}
      />,
    );
    const inputComponent = getByTestId('file-input');

    fireEvent.change(inputComponent, {
      target: { files: [{ name: 'csv', size: '120843092842123', type: 'text/badtype' }] },
    });

    expect(queryByText('Failed to Import file, the format is not supported')).toBeInTheDocument();
    expect(setErrorStepMock).toHaveBeenCalledWith(0);
  });
});
