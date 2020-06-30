import React from 'react';
import { fireEvent, render, within } from '@testing-library/react';

import ChartConfigModal from '../ChartConfigModal';
import useFetch from '../../../hook/useFetch';

jest.mock('../../../hook/useFetch');
describe('<ChartConfigModal />', () => {
  const props = {
    onCancel: jest.fn(),
    onOk: jest.fn(),
    open: true,
  };

  beforeEach(() => {
    useFetch.mockReturnValue({ headers: ['x-header', 'y-header'] });
  });

  it('should match a snapshot for <ChartConfigModal />', () => {
    render(<ChartConfigModal {...props} />);

    expect(document.querySelector('.MuiPaper-root')).toMatchSnapshot();
  });

  it('should provide selected chart config to onOK callback', () => {
    render(<ChartConfigModal {...props} />);
    const configModal = within(document.querySelector('.MuiPaper-root'));

    const xAxisDropDown = configModal.getByTestId('dropdown-x');
    fireEvent.mouseDown(within(xAxisDropDown).getByRole('button'));
    const optionListX = within(document.querySelector('ul'));
    fireEvent.click(optionListX.getByText('x-header'));

    const yAxisDropDown = configModal.getByTestId('dropdown-y');
    fireEvent.mouseDown(within(yAxisDropDown).getByRole('button'));
    const optionListY = within(document.querySelectorAll('ul')[1]);
    fireEvent.click(optionListY.getByText('y-header'));

    const okButton = configModal.getByText(/Ok/i);
    fireEvent.click(okButton);

    expect(props.onOk).toHaveBeenCalledWith({ xColumn: 'x-header', yColumn: 'y-header' });
  });

  it('should close select config modal on cancel click', () => {
    render(<ChartConfigModal {...props} />);
    const configModal = within(document.querySelector('.MuiPaper-root'));

    const cancelButton = configModal.getByText(/Cancel/i);
    fireEvent.click(cancelButton);

    expect(props.onCancel).toHaveBeenCalled();
  });
});
