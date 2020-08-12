import React from 'react';
import { fireEvent, render } from '@testing-library/react';

import DashboardLayout from '../DashboardLayout';
import useFetch from '../../../hook/useFetch';

jest.mock('../../chartConfigModal/ChartConfigModal', () => ({ open, onCancel, onOk }) => (
  <>
    <span>
      Modal Open
      {`${open.toString()}`}
    </span>
    <button type="button" onClick={onOk}>
      Ok
    </button>
    <button type="button" onClick={onCancel}>
      Cancel
    </button>
  </>
));

jest.mock('../../../uiComponent/FileUpload', () => (props) => (
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
  });

  it('should match a snapshot for <DashboardLayout />', () => {
    const { container } = render(<DashboardLayout />);

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
});
