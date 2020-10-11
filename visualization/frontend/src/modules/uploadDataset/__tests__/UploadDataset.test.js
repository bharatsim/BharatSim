import React from 'react';
import { render } from '@testing-library/react';
import { fireEvent } from '@testing-library/dom';
import UploadDataset from '../UploadDataset';
import withThemeProvider from '../../../theme/withThemeProvider';

import * as fileUtils from '../../../utils/fileUploadUtils';

jest.spyOn(fileUtils, 'parseCsv').mockImplementation((csvFile, onComplete) => {
  const data = { data: [{ col1: 'row1', col2: 1 }], errors: [] };
  onComplete(data);
});

const Component = withThemeProvider(UploadDataset);
describe('Upload Dataset', () => {
  it('should match snapshot for upload dataset component', () => {
    const { container } = render(<Component />);

    expect(container).toMatchSnapshot();
  });

  it('should display import file screen for step 1', () => {
    const { getByText } = render(<Component />);

    expect(getByText('Drag your file here or')).toBeInTheDocument();
  });

  it('should import file and open configure dataset step ', async () => {
    const { getByText, getByTestId, findByText } = render(<Component />);
    const inputComponent = getByTestId('file-input');

    fireEvent.change(inputComponent, { target: { files: [{ name: 'csv', size: '10' }] } });

    await findByText('Configure Datatype Component');

    expect(getByText('Configure Datatype Component')).toBeInTheDocument();
  });
});
