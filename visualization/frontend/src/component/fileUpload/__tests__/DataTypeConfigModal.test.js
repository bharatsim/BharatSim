import { render, within } from '@testing-library/react';
import React from 'react';
import { fireEvent } from '@testing-library/dom';
import DataTypeConfigModal from '../DataTypeConfigModal';
import { selectDropDownOption } from '../../../testUtil';

describe('DataTypeConfigModal', () => {
  const props = {
    dataRow: { col1: 1, col2: 'a', col3: '3' },
    isOpen: true,
    closeModal: jest.fn(),
    onApply: jest.fn(),
    onCancel: jest.fn(),
  };

  beforeEach(() => {
    render(<DataTypeConfigModal {...props} />);
  });

  it('should match a snapshot', () => {
    const container = document.querySelector('.MuiPaper-root');

    expect(container).toMatchSnapshot();
  });
  it('should close the modal on click of cancel', () => {
    const container = within(document.querySelector('.MuiPaper-root'));
    const { getByText } = container;
    const cancelButton = getByText('cancel');

    fireEvent.click(cancelButton);

    expect(props.onCancel).toHaveBeenCalled();
  });
  it('should call apply function on click of apply and upload button with default schema', () => {
    const container = within(document.querySelector('.MuiPaper-root'));
    const { getByText } = container;
    const applyButton = getByText('apply and upload');

    fireEvent.click(applyButton);

    expect(props.onApply).toHaveBeenCalledWith({ col1: 'Number', col2: 'String', col3: 'String' });
  });

  it('should call apply function with updated schema', () => {
    const container = within(document.querySelector('.MuiPaper-root'));
    const { getByText } = container;
    const applyButton = getByText('apply and upload');

    selectDropDownOption(container, 'col1', 'String');
    fireEvent.click(applyButton);

    expect(props.onApply).toHaveBeenCalledWith({ col1: 'String', col2: 'String', col3: 'String' });
  });
});
