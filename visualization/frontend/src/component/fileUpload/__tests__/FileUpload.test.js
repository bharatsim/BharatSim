import React from 'react';
import { fireEvent, render } from '@testing-library/react';

import { waitFor } from '@testing-library/dom';
import FileUpload from '../FileUpload';
import * as fetch from '../../../utils/fetch';

jest.mock('../../../utils/fetch');

describe('<FileUpload />', () => {
  it('should match a snapshot', () => {
    const { container } = render(<FileUpload />);

    expect(container).toMatchSnapshot();
  });

  it('should enable upload button if csv file is upload', () => {
    const { getByTestId } = render(<FileUpload />);
    const fileInput = getByTestId(/input-upload-file/);
    const uploadButton = getByTestId('button-upload');

    fireEvent.change(fileInput, {
      target: {
        files: [{ name: 'test.csv', type: 'text/csv' }],
      },
    });

    expect(uploadButton).not.toBeDisabled();
  });

  it('should disable upload button if other than csv file is upload', () => {
    const { getByTestId } = render(<FileUpload />);
    const fileInput = getByTestId(/input-upload-file/);
    const uploadButton = getByTestId('button-upload');

    fireEvent.change(fileInput, {
      target: {
        files: [{ name: 'test.jpg', type: 'image/jpg' }],
      },
    });

    expect(uploadButton).toBeDisabled();
  });

  it('should show error message if other than csv file is upload', async () => {
    const { getByTestId, getByText } = render(<FileUpload />);
    const fileInput = getByTestId(/input-upload-file/);

    fireEvent.change(fileInput, {
      target: {
        files: [{ name: 'test.jpg', type: 'image/jpg' }],
      },
    });

    const errorText = getByText('Only csv files are allowed, Please upload csv');
    expect(errorText).toBeInTheDocument();
  });

  it('should not show error message if csv file is upload', async () => {
    const { getByTestId, queryByText } = render(<FileUpload />);
    const fileInput = getByTestId(/input-upload-file/);

    fireEvent.change(fileInput, {
      target: {
        files: [{ name: 'test.csv', type: 'text/csv' }],
      },
    });

    const errorText = queryByText('Only csv files are allowed, Please upload csv');
    expect(errorText).toBe(null);
  });

  it('should send data on click of upload button', async () => {
    const { getByTestId } = render(<FileUpload />);
    const fileInput = getByTestId(/input-upload-file/);
    const uploadButton = getByTestId('button-upload');
    fetch.uploadFile.mockResolvedValue('success');

    fireEvent.change(fileInput, {
      target: {
        files: [{ name: 'test.csv', type: 'text/csv' }],
      },
    });

    fireEvent.click(uploadButton);

    expect(fetch.uploadFile).toHaveBeenCalledWith({
      file: { name: 'test.csv', type: 'text/csv' },
      url: '/api/dataSources',
    });
  });

  it('should reset file input', async () => {
    fetch.uploadFile.mockResolvedValue('success');
    const { getByTestId } = render(<FileUpload />);
    const fileInput = getByTestId(/input-upload-file/);
    const uploadButton = getByTestId('button-upload');
    fetch.uploadFile.mockResolvedValue('success');

    fireEvent.change(fileInput, {
      target: {
        files: [{ name: 'test.csv', type: 'text/csv' }],
      },
    });

    fireEvent.click(uploadButton);

    expect(fileInput.value).toBe('');
  });

  it('should display uploading message after file is uploaded', async () => {
    const { getByTestId, queryByText } = render(<FileUpload />);
    const fileInput = getByTestId(/input-upload-file/);
    const uploadButton = getByTestId('button-upload');
    fetch.uploadFile.mockResolvedValue('success');

    fireEvent.change(fileInput, {
      target: {
        files: [{ name: 'test.csv', type: 'text/csv' }],
      },
    });

    fireEvent.click(uploadButton);

    const uploading = queryByText('uploading');
    expect(uploading).toBeInTheDocument();
  });

  it('should display success message after file is uploaded', async () => {
    const { getByTestId, queryByText, findByText } = render(<FileUpload />);
    const fileInput = getByTestId(/input-upload-file/);
    const uploadButton = getByTestId('button-upload');
    fetch.uploadFile.mockResolvedValue('success');

    fireEvent.change(fileInput, {
      target: {
        files: [{ name: 'test.csv', type: 'text/csv' }],
      },
    });

    fireEvent.click(uploadButton);

    await waitFor(() => findByText('file uploded'));

    const uploaded = queryByText('file uploded');
    expect(uploaded).toBeInTheDocument();
  });

  it('should display error message after file uploading failed', async () => {
    const { getByTestId, queryByText, findByText } = render(<FileUpload />);
    const fileInput = getByTestId(/input-upload-file/);
    const uploadButton = getByTestId('button-upload');
    fetch.uploadFile.mockRejectedValueOnce('failed');

    fireEvent.change(fileInput, {
      target: {
        files: [{ name: 'test.csv', type: 'text/csv' }],
      },
    });

    fireEvent.click(uploadButton);

    await waitFor(() => findByText('file not upload'));

    const uploadFiled = queryByText('file not upload');
    expect(uploadFiled).toBeInTheDocument();
  });
});
