/* eslint-disable no-console */
import React from 'react';
import { act, fireEvent, render, waitFor, within } from '@testing-library/react';

import FileUpload from '../FileUpload';
import * as fileUtils from '../../../utils/fileUploadUtils';
import { api } from '../../../utils/api';

jest.mock('../../../utils/api', () => ({
  api: {
    uploadFileAndSchema: jest.fn(),
  },
}));

jest.spyOn(fileUtils, 'parseCsv').mockImplementation((csvFile, onComplete) => {
  const data = { data: [{ col1: 'row1', col2: 1 }] };
  onComplete(data);
});

describe('<FileUpload />', () => {
  let renderedComponent;
  let fileInput;
  let uploadButton;

  beforeEach(() => {
    renderedComponent = render(<FileUpload />);
    fileInput = renderedComponent.getByTestId(/input-upload-file/);
    uploadButton = renderedComponent.getByTestId('button-upload');
    api.uploadFileAndSchema.mockResolvedValue('success');
  });

  it('should match a snapshot', () => {
    const { container } = renderedComponent;

    expect(container).toMatchSnapshot();
  });

  it('should enable upload button if csv file is upload', () => {
    fireEvent.change(fileInput, {
      target: {
        files: [{ name: 'test.csv', type: 'text/csv', size: 2123 }],
      },
    });

    expect(uploadButton).not.toBeDisabled();
  });

  it('should disable upload button if other than csv file is upload', () => {
    fireEvent.change(fileInput, {
      target: {
        files: [{ name: 'test.jpg', type: 'image/jpg' }],
      },
    });

    expect(uploadButton).toBeDisabled();
  });

  it('should show error message if other than csv file is upload', async () => {
    const { getByText } = renderedComponent;

    fireEvent.change(fileInput, {
      target: {
        files: [{ name: 'test.jpg', type: 'image/jpg' }],
      },
    });

    const errorText = getByText('Please upload valid csv file');
    expect(errorText).toBeInTheDocument();
  });

  it('should not show error message if valid csv file is upload', async () => {
    const { queryByText } = renderedComponent;

    fireEvent.change(fileInput, {
      target: {
        files: [{ name: 'test.csv', type: 'text/csv' }],
      },
    });

    const errorText = queryByText(
      'Only csv files are allowed of maxmimum size 10MB, Please upload valid csv a file',
    );
    expect(errorText).toBe(null);
  });

  it('should open datatype config modal for selected file on click of upload button', async () => {
    api.uploadFileAndSchema.mockResolvedValue('success');

    fireEvent.change(fileInput, {
      target: {
        files: [{ name: 'test.csv', type: 'text/csv' }],
      },
    });

    await act(async () => {
      fireEvent.click(uploadButton);
    });

    const container = within(document.querySelector('.MuiPaper-root'));
    expect(container.queryByText('Configure Datatype')).toBeInTheDocument();
  });

  it('should upload file and metadata of csv file at upload api', async () => {
    api.uploadFileAndSchema.mockResolvedValue('success');

    fireEvent.change(fileInput, {
      target: {
        files: [{ name: 'test.csv', type: 'text/csv' }],
      },
    });

    fireEvent.click(uploadButton);
    const Modal = within(document.querySelector('.MuiPaper-root'));
    const { getByText } = Modal;

    const applyAndUploadButton = getByText('apply and upload');

    await act(async () => {
      fireEvent.click(applyAndUploadButton);
    });

    expect(api.uploadFileAndSchema).toHaveBeenCalledWith({
      file: {
        name: 'test.csv',
        type: 'text/csv',
      },
      schema: { col1: 'String', col2: 'Number' },
    });
  });

  it('should display uploading message while file is uploading', async () => {
    const { queryByText } = renderedComponent;

    api.uploadFileAndSchema.mockResolvedValue('success');

    fireEvent.change(fileInput, {
      target: {
        files: [{ name: 'test.csv', type: 'text/csv' }],
      },
    });

    fireEvent.click(uploadButton);
    const Modal = within(document.querySelector('.MuiPaper-root'));
    const { getByText } = Modal;

    const applyAndUploadButton = getByText('apply and upload');
    fireEvent.click(applyAndUploadButton);

    const uploading = queryByText('uploading test.csv');
    await waitFor(() => expect(uploading).toBeInTheDocument());
  });

  it('should display success message after file is uploaded', async () => {
    const { queryByText, findByText } = renderedComponent;

    api.uploadFileAndSchema.mockResolvedValue('success');

    fireEvent.change(fileInput, {
      target: {
        files: [{ name: 'test.csv', type: 'text/csv' }],
      },
    });

    fireEvent.click(uploadButton);

    const Modal = within(document.querySelector('.MuiPaper-root'));
    const { getByText } = Modal;

    const applyAndUploadButton = getByText('apply and upload');

    await act(async () => {
      fireEvent.click(applyAndUploadButton);
    });

    await findByText('test.csv successfully uploaded');
    const uploaded = queryByText('test.csv successfully uploaded');
    await waitFor(() => expect(uploaded).toBeInTheDocument());
  });

  it('should display error message after file uploading failed', async () => {
    const { queryByText, findByText } = renderedComponent;

    api.uploadFileAndSchema.mockRejectedValueOnce('failed');

    fireEvent.change(fileInput, {
      target: {
        files: [{ name: 'test.csv', type: 'text/csv' }],
      },
    });
    fireEvent.click(uploadButton);

    const Modal = within(document.querySelector('.MuiPaper-root'));
    const { getByText } = Modal;

    const applyAndUploadButton = getByText('apply and upload');

    await act(async () => {
      fireEvent.click(applyAndUploadButton);
    });

    await findByText('Error occurred while unloading test.csv');
    const uploadFiled = queryByText('Error occurred while unloading test.csv');
    await waitFor(() => expect(uploadFiled).toBeInTheDocument());
  });

  it('should reset file upload status after adding new file', async () => {
    const { queryByText, findByText } = renderedComponent;

    api.uploadFileAndSchema.mockRejectedValueOnce('failed');

    fireEvent.change(fileInput, {
      target: {
        files: [{ name: 'test.csv', type: 'text/csv' }],
      },
    });
    fireEvent.click(uploadButton);

    const Modal = within(document.querySelector('.MuiPaper-root'));
    const { getByText } = Modal;

    const applyAndUploadButton = getByText('apply and upload');

    await act(async () => {
      fireEvent.click(applyAndUploadButton);
    });

    await findByText('Error occurred while unloading test.csv');
    const uploadFiled = queryByText('Error occurred while unloading test.csv');
    await waitFor(() => expect(uploadFiled).toBeInTheDocument());

    fireEvent.change(fileInput, {
      target: {
        files: [{ name: 'test.csv', type: 'text/csv' }],
      },
    });

    expect(uploadFiled).not.toBeInTheDocument();
  });

  it('should reset file input after successful upload', async () => {
    api.uploadFileAndSchema.mockResolvedValue('success');

    fireEvent.change(fileInput, {
      target: {
        files: [{ name: 'test.csv', type: 'text/csv' }],
      },
    });

    fireEvent.click(uploadButton);
    const Modal = within(document.querySelector('.MuiPaper-root'));
    const { getByText } = Modal;

    const applyAndUploadButton = getByText('apply and upload');

    await act(async () => {
      fireEvent.click(applyAndUploadButton);
    });

    expect(fileInput.files).toBe(null);
    expect(fileInput.value).toBe('');
  });

  it('should reset file input on click of cancel button', async () => {
    fireEvent.change(fileInput, {
      target: {
        files: [{ name: 'test.csv', type: 'text/csv' }],
      },
    });

    fireEvent.click(uploadButton);

    const Modal = within(document.querySelector('.MuiPaper-root'));
    const { getByText } = Modal;

    const cancel = getByText('cancel');

    fireEvent.click(cancel);

    expect(fileInput.files).toBe(null);
    expect(fileInput.value).toBe('');
  });
});
