import React from 'react';
import { act, fireEvent, render } from '@testing-library/react';
import DashboardLayout from '../DashboardLayout';
import useFetch from '../../../hook/useFetch';
import * as fetch from '../../../utils/fetch';

jest.spyOn(fetch, 'uploadData');
jest.spyOn(fetch, 'fetchData');

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

jest.mock('../../charts/barChart/BarChart', () => (props) => (
  <div>
    Bar Chart
    {/* eslint-disable-next-line no-undef */}
    <span>{mockPropsCapture(props)}</span>
  </div>
));

jest.mock('../../charts/lineChart/LineChart', () => (props) => (
  <div>
    Line Chart
    {/* eslint-disable-next-line no-undef */}
    <span>{mockPropsCapture(props)}</span>
  </div>
));

jest.mock('../../../hook/useFetch');

describe('<DashboardLayout />', () => {
  beforeEach(() => {
    useFetch.mockReturnValue({ headers: ['x-header', 'y-header'] });
    fetch.uploadData.mockResolvedValue({ dashboardId: 'id' });
    fetch.fetchData.mockResolvedValue({ dashboards: [] });
  });
  afterEach(() => {
    jest.clearAllMocks();
  });

  it('should match a snapshot for <DashboardLayout />', () => {
    const { container } = render(<DashboardLayout />);

    expect(container).toMatchSnapshot();
  });
  it('should match a snapshot for <DashboardLayout /> if dashboard is loaded with data', async () => {
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
    fetch.fetchData.mockResolvedValue({ dashboards: [data] });

    const { container, findByTestId } = render(<DashboardLayout />);

    await findByTestId('widget-0');

    expect(container).toMatchSnapshot();
  });

  it('should open modal on click of cancel button ', () => {
    const { getByText } = render(<DashboardLayout />);

    const addWidgetButton = getByText(/Line chart/i);
    fireEvent.click(addWidgetButton);

    expect(getByText(/Modal Open/i)).toHaveTextContent(/true/i);
  });

  it('should closed modal on click of cancel button ', async () => {
    const { getByText, queryByText } = render(<DashboardLayout />);

    const addWidgetButton = getByText(/Line Chart/i);
    fireEvent.click(addWidgetButton);

    const cancelButton = getByText(/Cancel/i);
    fireEvent.click(cancelButton);

    expect(await queryByText(/Modal Open/i)).toBeNull();
  });

  it('should add new widget with line chart', () => {
    const { getByText, getByTestId } = render(<DashboardLayout />);

    const addWidgetButton = getByText(/Line Chart/i);
    fireEvent.click(addWidgetButton);

    const okButton = getByText(/Ok/i);
    fireEvent.click(okButton);

    const widget = getByTestId('widget-0');

    expect(widget).toBeInTheDocument();
    expect(widget).toMatchSnapshot();
  });
  it('should add new widget with bar chart', () => {
    const { getByText, getByTestId } = render(<DashboardLayout />);

    const addWidgetButton = getByText(/Bar Chart/i);
    fireEvent.click(addWidgetButton);

    const okButton = getByText(/Ok/i);
    fireEvent.click(okButton);

    const widget = getByTestId('widget-0');

    expect(widget).toBeInTheDocument();
    expect(widget).toMatchSnapshot();
  });
  it('should save dashboard on click of save dashboard button with empty widgets', async () => {
    const { getByText } = render(<DashboardLayout />);
    const saveDashboard = getByText(/Save Dashboard/i);
    const requestObject = {
      data: JSON.stringify({
        dashboardData: {
          widgets: [],
          layout: [],
          dashboardId: null,
          name: 'dashboard1',
          count: 0,
        },
      }),
      headers: { 'content-type': 'application/json' },
      url: '/api/dashboard',
    };

    await act(async () => {
      fireEvent.click(saveDashboard);
    });

    expect(fetch.uploadData).toHaveBeenCalledWith(requestObject);
  });
  it('should save dashboard with widgets on click of save dashboard button ', async () => {
    const { getByText } = render(<DashboardLayout />);
    const saveDashboard = getByText(/Save Dashboard/i);

    const requestObject = {
      data: JSON.stringify({
        dashboardData: {
          widgets: [
            {
              config: 'config',
              chartType: 'barChart',
              layout: { i: 'widget-0', x: 0, y: null, w: 2, h: 2 },
            },
          ],
          layout: [{ w: 2, h: 2, x: 0, y: 0, i: 'widget-0', moved: false, static: false }],
          dashboardId: null,
          name: 'dashboard1',
          count: 1,
        },
      }),
      headers: { 'content-type': 'application/json' },
      url: '/api/dashboard',
    };

    const addWidgetButton = getByText(/Bar Chart/i);
    fireEvent.click(addWidgetButton);

    const okButton = getByText(/Ok/i);
    fireEvent.click(okButton);

    await act(async () => {
      fireEvent.click(saveDashboard);
    });
    expect(fetch.uploadData).toHaveBeenCalledWith(requestObject);
  });
});
