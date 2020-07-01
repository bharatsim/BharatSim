import React from 'react';
import { fireEvent, render, within } from '@testing-library/react';

import DashboardLayout from '../DashboardLayout';
import useFetch from '../../../hook/useFetch';

jest.mock('../../lineChart/LineChart', () => (props) => (
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

  it('should add new widget', () => {
    const { getByText, getByTestId } = render(<DashboardLayout />);

    const addWidgetButton = getByText(/Add widget/i);
    fireEvent.click(addWidgetButton);

    const configModal = within(document.querySelector('.MuiPaper-root'));

    const xAxisDropDown = configModal.getByTestId('dropdown-x');
    fireEvent.mouseDown(within(xAxisDropDown).getByRole('button'));
    const optionListX = within(document.querySelector('ul'));
    fireEvent.click(optionListX.getByText(/x-header/i));

    const yAxisDropDown = configModal.getByTestId('dropdown-y');
    fireEvent.mouseDown(within(yAxisDropDown).getByRole('button'));
    const optionListY = within(document.querySelectorAll('ul')[1]);
    fireEvent.click(optionListY.getByText(/y-header/i));

    const okButton = configModal.getByText(/Ok/i);
    fireEvent.click(okButton);

    const widget = getByTestId('widget-0');

    expect(widget).toBeInTheDocument();
    expect(widget).toMatchSnapshot();
  });
});
