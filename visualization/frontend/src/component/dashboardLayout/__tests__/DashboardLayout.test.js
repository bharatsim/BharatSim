import React from 'react';
import { act, fireEvent, render } from '@testing-library/react';
import DashboardLayout from '../DashboardLayout';

import useFetch from '../../../hook/useFetch';
import { api } from '../../../utils/api';

jest.mock('../../../hook/useFetch');
jest.mock('../../../utils/api', () => ({
  api: {
    saveDashboard: jest.fn(),
  },
}));

jest.mock('../../chartConfigModal/ChartConfigModal', () => ({ open, onCancel, onOk }) => (
  <>
    <span>
      Modal Open
      {`${open.toString()}`}
    </span>
    <button type="button" onClick={() => onOk('config')}>
      Ok
    </button>
    <button type="button" onClick={onCancel}>
      Cancel
    </button>
  </>
));

jest.mock('../../fileUpload/FileUpload', () => (props) => (
  <div>
    File Upload
    {/* eslint-disable-next-line no-undef */}
    <span>{mockPropsCapture(props)}</span>
  </div>
));

jest.mock('../../../modules/charts/barChart/BarChart', () => (props) => (
  <div>
    Bar Chart
    {/* eslint-disable-next-line no-undef */}
    <span>{mockPropsCapture(props)}</span>
  </div>
));

jest.mock('../../../modules/charts/lineChart/LineChart', () => (props) => (
  <div>
    Line Chart
    {/* eslint-disable-next-line no-undef */}
    <span>{mockPropsCapture(props)}</span>
  </div>
));

describe('<DashboardLayout />', () => {
  beforeEach(() => {
    useFetch.mockReturnValue({ data: { dashboards: [] }, loadingState: 'SUCCESS' });
    api.saveDashboard.mockResolvedValue({ dashboardId: 'id' });
  });
  afterEach(() => {
    jest.clearAllMocks();
  });

  it('should match a snapshot for <DashboardLayout /> at initial state', async () => {
    const { container } = render(<DashboardLayout />);

    await act(async () => {
      expect(container).toMatchSnapshot();
    });
  });

  it('should match a snapshot for <DashboardLayout /> when dashboard is loaded with data', async () => {
    const data = {
      name: 'dashboard1',
      widgets: [
        {
          layout: { i: 'widget-0', x: 0, y: null, w: 2, h: 2 },
          config: 'config',
          chartType: 'barChart',
        },
      ],
      dashboardId: null,
      layout: [{ w: 2, h: 2, x: 0, y: 0, i: 'widget-0', moved: false, static: false }],
    };

    useFetch.mockReturnValue({ data: { dashboards: [data] }, loadingState: 'SUCCESS' });

    const { container } = render(<DashboardLayout />);

    expect(container).toMatchSnapshot();
  });

  it('should show loader while fetching data for <DashboardLayout />', async () => {
    useFetch.mockReturnValue({ data: undefined, loadingState: 'LOADING' });

    const { container } = render(<DashboardLayout />);

    expect(container).toMatchSnapshot();
  });

  it('should show Error while fetching data for <DashboardLayout /> if error occurred', async () => {
    useFetch.mockReturnValue({ data: undefined, loadingState: 'ERROR' });

    const { container } = render(<DashboardLayout />);

    expect(container).toMatchSnapshot();
  });

  it('should open modal on click of add line chart button ', async () => {
    const { getByText } = render(<DashboardLayout />);

    const addWidgetButton = getByText(/Line chart/i);
    fireEvent.click(addWidgetButton);

    await act(async () => {
      expect(getByText(/Modal Open/i)).toHaveTextContent(/true/i);
    });
  });

  it('should closed modal on click of cancel button ', async () => {
    const { getByText, queryByText } = render(<DashboardLayout />);

    const addWidgetButton = getByText(/Line Chart/i);
    fireEvent.click(addWidgetButton);

    const cancelButton = getByText(/Cancel/i);
    fireEvent.click(cancelButton);

    await act(async () => {
      expect(queryByText(/Modal Open/i)).toBeNull();
    });
  });

  it('should add new widget with line chart', async () => {
    const { getByText, getByTestId } = render(<DashboardLayout />);

    const addWidgetButton = getByText(/Line Chart/i);
    fireEvent.click(addWidgetButton);

    const okButton = getByText(/Ok/i);
    fireEvent.click(okButton);

    const widget = getByTestId('widget-0');

    await act(async () => {
      expect(widget).toBeInTheDocument();
      expect(widget).toMatchSnapshot();
    });
  });

  it('should save dashboard on click of save dashboard button with empty widgets', async () => {
    const { getByText } = render(<DashboardLayout />);
    const saveDashboard = getByText(/Save Dashboard/i);
    const expectedDashboardData = {
      widgets: [],
      layout: [],
      dashboardId: null,
      name: 'dashboard1',
      count: 0,
    };

    await act(async () => {
      fireEvent.click(saveDashboard);
    });

    expect(api.saveDashboard).toHaveBeenCalledWith(expectedDashboardData);
  });

  it('should save dashboard with widgets on click of save dashboard button ', async () => {
    const { getByText } = render(<DashboardLayout />);
    const saveDashboard = getByText(/Save Dashboard/i);

    const expectedDashboardData = {
      widgets: [
        {
          config: 'config',
          chartType: 'barChart',
          layout: { i: 'widget-0', x: 0, y: Infinity, w: 6, h: 2 },
        },
      ],
      layout: [{ w: 6, h: 2, x: 0, y: 0, i: 'widget-0', moved: false, static: false }],
      dashboardId: null,
      name: 'dashboard1',
      count: 1,
    };

    const addWidgetButton = getByText(/Bar Chart/i);
    fireEvent.click(addWidgetButton);

    const okButton = getByText(/Ok/i);
    fireEvent.click(okButton);

    await act(async () => {
      fireEvent.click(saveDashboard);
    });

    expect(api.saveDashboard).toHaveBeenCalledWith(expectedDashboardData);
  });

  it('should show message for successful saving of dashboard', async () => {
    const { getByText } = render(<DashboardLayout />);
    const saveDashboard = getByText(/Save Dashboard/i);

    await act(async () => {
      fireEvent.click(saveDashboard);
    });

    expect(getByText(/Dashboard dashboard1 Saved Successfully/)).toBeInTheDocument();
  });

  it('should show error while saving dashboard failed', async () => {
    const { getByText } = render(<DashboardLayout />);
    const saveDashboard = getByText(/Save Dashboard/i);
    api.saveDashboard.mockRejectedValue('error');

    await act(async () => {
      fireEvent.click(saveDashboard);
    });

    expect(getByText(/Failed to save dashboard/)).toBeInTheDocument();
  });
});
