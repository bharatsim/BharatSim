import React from 'react';
import { render } from '@testing-library/react';
import Table from '../table/Table';
import withThemeProvider from '../../theme/withThemeProvider';
import tableIcons from '../table/tableIcon';

const Component = withThemeProvider(() => (
  <Table
    columns={[
      { title: 'Adı', field: 'name', type: 'string' },
      { title: 'Soyadı', field: 'surname', type: 'string' },
      { title: 'Doğum Yılı', field: 'birthYear', type: 'numeric' },
      { title: 'Doğum Yeri', field: 'birthCity', type: 'string' },
    ]}
    data={[
      { name: 'Mehmet', surname: 'Baran', birthYear: 1987, birthCity: 63 },
      { name: 'Mehmet', surname: 'Baran', birthYear: 1987, birthCity: 63 },
    ]}
    title="Table"
    components={{
      Pagination: () => <td>Pagination</td>,
    }}
  />
));

describe('<Table />', () => {
  it('should match snapshot', () => {
    const { container } = render(<Component />);

    expect(container).toMatchSnapshot();
  });
});

describe('TableIcons', () => {
  Object.keys(tableIcons).forEach((tableIconName) => {
    it(`table icon ${tableIconName}`, () => {
      const Icon = tableIcons[tableIconName];
      const { container } = render(<Icon />);

      expect(container).toMatchSnapshot();
    });
  });
});
