import React from 'react';
import { fireEvent, render } from '@testing-library/react';

import FileInput from '../FileInput';

describe('<FileInput />', () => {
  it('should match snapshot', () => {
    const { container } = render(<FileInput onChange={jest.fn()} />);

    expect(container).toMatchSnapshot();
  });

  it('should display file upload status', () => {
    const { queryByText } = render(
      <FileInput
        onChange={jest.fn()}
        fileUploadStatus="loading"
        fileUploadStatusMessage="Loading..."
      />,
    );

    expect(queryByText('Loading...')).toBeInTheDocument();
  });

  it('should display file upload status for error with error class', () => {
    const { queryByText } = render(
      <FileInput
        onChange={jest.fn()}
        fileUploadStatus="error"
        fileUploadStatusMessage="error while upload"
      />,
    );

    expect(queryByText('error while upload')).toMatchSnapshot();
  });

  it('should display validation error if selected object is not file', () => {
    const { queryByText } = render(
      <FileInput onChange={jest.fn()} error="Please upload valid csv a file" />,
    );

    expect(queryByText('Please upload valid csv a file')).toBeInTheDocument();
  });

  it('should pass selected file to onChange callback function', () => {
    const onChange = jest.fn();
    const { getByTestId } = render(<FileInput onChange={onChange} />);

    const fileInput = getByTestId(/input-upload-file/);

    fireEvent.change(fileInput, {
      target: {
        files: [{ name: 'test.csv', type: 'text/csv', size: 2123 }],
      },
    });

    expect(onChange).toHaveBeenCalledWith({ name: 'test.csv', type: 'text/csv', size: 2123 });
  });
});
