import React from 'react';
import { render } from '@testing-library/react';
import { fireEvent } from '@testing-library/dom';
import withThemeProvider from '../../../theme/withThemeProvider';

import * as fileUtils from '../../../utils/fileUploadUtils';
import { ProjectLayoutProvider } from '../../../contexts/projectLayoutContext';
import UploadDataset from '../UploadDataset';

jest.spyOn(fileUtils, 'parseCsv').mockImplementation((csvFile, onComplete) => {
  const data = { data: [{ col1: 'row1', col2: 1 }], errors: [] };
  onComplete(data);
});

const ComponentWithProvider = withThemeProvider(() => (
  <ProjectLayoutProvider
    value={{
      projectMetadata: { name: 'project1', id: '123' },
      selectedDashboardMetadata: { name: 'dashboard1' },
    }}
  >
    <UploadDataset />
  </ProjectLayoutProvider>
));

describe('Upload Dataset', () => {
  it('should match snapshot for upload dataset component', () => {
    const { container } = render(<ComponentWithProvider />);

    expect(container).toMatchSnapshot();
  });

  it('should display import file screen for step 1', () => {
    const { getByText } = render(<ComponentWithProvider />);

    expect(getByText('Drag your file here or')).toBeInTheDocument();
  });

  it('should import file and open configure dataset step ', async () => {
    const { getByText, getByTestId, findByText } = render(<ComponentWithProvider />);
    const inputComponent = getByTestId('file-input');

    fireEvent.change(inputComponent, { target: { files: [new File([''], 'testFile.csv')] } });

    await findByText('DataFile:');

    expect(getByText('testFile.csv')).toBeInTheDocument();
  });
});
