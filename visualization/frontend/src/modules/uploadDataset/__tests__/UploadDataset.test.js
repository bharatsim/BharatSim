import React from 'react';
import { render } from '@testing-library/react';
import { fireEvent } from '@testing-library/dom';

import * as fileUtils from '../../../utils/fileUploadUtils';
import withThemeProvider from '../../../theme/withThemeProvider';
import { ProjectLayoutProvider } from '../../../contexts/projectLayoutContext';
import UploadDataset from '../UploadDataset';
import { api } from '../../../utils/api';

jest.spyOn(fileUtils, 'parseCsv').mockImplementation((csvFile, onComplete) => {
  const data = { data: [{ col1: 'row1', col2: 1 }], errors: [] };
  onComplete(data);
});

const mockHistoryPush = jest.fn();

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useHistory: () => ({
    push: mockHistoryPush,
  }),
}));

jest.mock('../../../utils/api', () => ({
  api: {
    uploadFileAndSchema: jest.fn().mockResolvedValue('success'),
  },
}));

const ComponentWithProvider = withThemeProvider(() => (
  <ProjectLayoutProvider
    value={{
      projectMetadata: { name: 'project1', id: '123' },
      selectedDashboardMetadata: { name: 'dashboard1', _id: 'dashboardId' },
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

  it('should import file and open configure dataset step and upload data', async () => {
    const { getByText, getByTestId, findByText } = render(<ComponentWithProvider />);
    const inputComponent = getByTestId('file-input');

    fireEvent.change(inputComponent, { target: { files: [new File([''], 'testFile.csv')] } });

    await findByText('DataFile:');

    fireEvent.click(getByText('Upload'));

    await findByText('DataFile:');

    expect(api.uploadFileAndSchema).toHaveBeenCalledWith({
      dashboardId: 'dashboardId',
      file: expect.any(File),
      schema: { col1: 'String', col2: 'Number' },
    });
  });

  it('should navigate to configure-dashboard on click of cancel', async () => {
    const { getByText } = render(<ComponentWithProvider />);

    fireEvent.click(getByText('Cancel'));

    expect(mockHistoryPush).toHaveBeenCalledWith('configure-dataset');
  });

  it('should navigate to configure-dashboard on upload successful', async () => {
    const { getByText, getByTestId, findByText } = render(<ComponentWithProvider />);
    const inputComponent = getByTestId('file-input');

    fireEvent.change(inputComponent, { target: { files: [new File([''], 'testFile.csv')] } });

    await findByText('DataFile:');

    fireEvent.click(getByText('Upload'));

    await findByText('DataFile:');

    expect(mockHistoryPush).toHaveBeenCalledWith('configure-dataset');
  });
});
