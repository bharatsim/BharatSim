import React from 'react';
import { render } from '@testing-library/react';
import ConfigureDatatype from '../ConfigureDatatype';
import withThemeProvider from '../../../theme/withThemeProvider';

jest.mock('../../../uiComponent/table/Table');

const Component = withThemeProvider(() => (
  <ConfigureDatatype
    previewData={[
      { col1: 'abc', col2: 123 },
      { col1: 'abcd', col2: 1234 },
    ]}
    schema={{ col1: 'string', col2: 'number' }}
    selectedFile={new File([''], 'filename.txt', { type: 'text/plain' })}
  />
));

describe('<ConfigureDatatype />', () => {
  it('should match snapshot', () => {
    const { container } = render(<Component />);

    expect(container).toMatchSnapshot();
  });
});
