import React from 'react';
import { render } from '@testing-library/react';
import DashboardDataSetsTable from '../DashboardDataSetsTable';
import withThemeProvider from '../../../theme/withThemeProvider';

const mockData = [
  {
    createdAt: '2020-10-29T09:17:09.146Z',
    dashboardId: '5f9952ede93dbd234a39d82f',
    fileSize: 125005,
    fileType: 'text/csv',
    name: 'csv-file-name',
    updatedAt: '2020-10-29T09:17:09.146Z',
    _id: '5f9a88952629222105e180df',
  },
];

const Component = withThemeProvider(() => <DashboardDataSetsTable dataSources={mockData} />);

describe('<DashboardDataSetsTable />', () => {
  it('should render date in DD-MM-YYYY, hh:mm am/pm', () => {
    const { getByText } = render(<Component />);

    expect(getByText('29-Oct-2020 at 2:47 PM')).not.toBeNull();
  });

  it('should render file size in MB ', () => {
    const { getByText } = render(<Component />);

    expect(getByText('0.12MB')).not.toBeNull();
  });
});
