import React from 'react';
import { render } from '@testing-library/react';
import DashboardDataSetsTable from '../DashboardDataSetsTable';
import withThemeProvider from '../../../theme/withThemeProvider';

const mockData = [
  {
    createdAt: 'Fri Oct 20 2020 22:39:07 GMT+0530',
    dashboardId: '5f9952ede93dbd234a39d82f',
    fileSize: 125005,
    fileType: 'text/csv',
    name: 'csv-file-name',
    updatedAt: 'Fri Oct 20 2020 22:39:07 GMT+0530',
    _id: '5f9a88952629222105e180df',
  },
];

const Component = withThemeProvider(() => <DashboardDataSetsTable dataSources={mockData} />);

describe('<DashboardDataSetsTable />', () => {
  it('should render date in DD-MM-YYYY, hh:mm am/pm', () => {
    const { getByText } = render(<Component />);

    expect(getByText('20-Oct-2020 at 5:09 PM')).not.toBeNull();
  });

  it('should render file size in MB ', () => {
    const { getByText } = render(<Component />);

    expect(getByText('0.12MB')).not.toBeNull();
  });
});
