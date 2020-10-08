import React from 'react';
import { render } from '@testing-library/react';
import { fireEvent } from '@testing-library/dom';
import { UploadDataset } from '../UploadDataset';
import withThemeProvider from '../../../theme/withThemeProvider';

import * as fileUtils from '../../../utils/fileUploadUtils';

jest.spyOn(fileUtils, 'parseCsv').mockImplementation((csvFile, onComplete) => {
  const data = { data: [{ col1: 'row1', col2: 1 }], errors: [] };
  onComplete(data);
});

const Component = withThemeProvider(UploadDataset);
describe('Upload Dataser', () => {
  it('should match snapshot for upload dataset component', () => {
    const { container } = render(<Component />);
    expect(container).toMatchSnapshot();
  });

  it('should import file and open configure dataset step ', async () => {
    const { container, getByTestId, findByText } = render(<Component />);
    const inputComponent = getByTestId('file-input');
    fireEvent.change(inputComponent, { target: { files: [{ name: 'csv', size: '10' }] } });
    await findByText('Configure Datatype Component');
    expect(container).toMatchSnapshot();
  });
});
