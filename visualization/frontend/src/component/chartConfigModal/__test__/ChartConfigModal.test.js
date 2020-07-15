import React from 'react';

import { fireEvent, render, waitFor, within } from '@testing-library/react';

import ChartConfigModal from '../ChartConfigModal';
import fetch from '../../../utils/fetch';

jest.mock('../../../utils/url', () => ({
  url: {
    getHeaderUrl: jest.fn().mockReturnValue('/headers'),
    DATA_SOURCES: '/dataSources',
  },
}));

jest.mock('../../../utils/fetch', () => ({
  __esModule: true,
  default: jest.fn(({ url }) => {
    if (url === '/headers') {
      return Promise.resolve({ headers: ['x-header', 'y-header'] });
    }
    if (url === '/dataSources') {
      return Promise.resolve({ dataSources: ['modelone', 'modeltwo'] });
    }
    return Promise.reject();
  }),
}));

function selectDropDownOption(container, dropDownId, optionId) {
  const dropDown = container.getByTestId(dropDownId);
  fireEvent.mouseDown(within(dropDown).getByRole('button'));
  const options = within(within(document.getElementById(`menu-${dropDownId}`)).getByRole('listbox'));
  fireEvent.click(options.getByTestId(`${dropDownId}-${optionId}`));
}

describe('<ChartConfigModal />', () => {
  const props = {
    onCancel: jest.fn(),
    onOk: jest.fn(),
    open: true,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should match a snapshot for <ChartConfigModal />', async () => {
    render(<ChartConfigModal {...props} />);

    await waitFor(() => document.querySelector('.MuiPaper-root'));

    expect(document.querySelector('.MuiPaper-root')).toMatchSnapshot();
  });

  it('should provide selected chart config to onOK callback', async () => {
    render(<ChartConfigModal {...props} />);

    await waitFor(() => document.querySelector('.MuiPaper-root'));

    const configModal = within(document.querySelector('.MuiPaper-root'));

    selectDropDownOption(configModal, 'dropdown-dataSources', 'modelone');

    await waitFor(() => expect(fetch).toBeCalledTimes(2));

    selectDropDownOption(configModal, 'dropdown-x', 'x-header');

    selectDropDownOption(configModal, 'dropdown-y', 'y-header');

    const okButton = configModal.getByText(/Ok/i);

    fireEvent.click(okButton);

    expect(props.onOk).toHaveBeenCalledWith({ dataSource: 'modelone', xColumn: 'x-header', yColumn: 'y-header' });
  });

  it('should be disable x-axis and y-axis dropdown if data source is not selected', async () => {
    render(<ChartConfigModal {...props} />);

    await waitFor(() => document.querySelector('.MuiPaper-root'));

    const configModal = within(document.querySelector('.MuiPaper-root'));

    const xDropdown = within(configModal.getByTestId('dropdown-x'));
    const yDropdown = within(configModal.getByTestId('dropdown-y'));

    expect(xDropdown.getByRole('button')).toHaveAttribute('aria-disabled', 'true');
    expect(yDropdown.getByRole('button')).toHaveAttribute('aria-disabled', 'true');
  });

  it('should be enable x-axis and y-axis dropdown if data source selected', async () => {
    render(<ChartConfigModal {...props} />);

    await waitFor(() => document.querySelector('.MuiPaper-root'));

    const configModal = within(document.querySelector('.MuiPaper-root'));

    const xDropdown = within(configModal.getByTestId('dropdown-x'));
    const yDropdown = within(configModal.getByTestId('dropdown-y'));

    selectDropDownOption(configModal, 'dropdown-dataSources', 'modelone');

    await waitFor(() => {
      expect(xDropdown.getByRole('button')).toHaveAttribute('aria-disabled', 'true');
      expect(yDropdown.getByRole('button')).toHaveAttribute('aria-disabled', 'true');
    });
  });

  it('should close select config modal on cancel click', async () => {
    render(<ChartConfigModal {...props} />);

    await waitFor(() => document.querySelector('.MuiPaper-root'));

    const configModal = within(document.querySelector('.MuiPaper-root'));

    const cancelButton = configModal.getByText(/Cancel/i);
    fireEvent.click(cancelButton);

    expect(props.onCancel).toHaveBeenCalled();
  });

  it('should provide empty element if csv header is null', async () => {
    fetch.mockReturnValue(null);
    render(<ChartConfigModal {...props} />);

    await waitFor(() => {
      expect(document.querySelector('.MuiPaper-root')).toBeNull();
    });
  });

  it('should display message of no data source preset if api return empty datasource array', async () => {
    fetch.mockReturnValue({ dataSources: [] });
    render(<ChartConfigModal {...props} />);

    await waitFor(() => {
      expect(document.querySelector('.MuiPaper-root')).toHaveTextContent(/No data source present, upload data source/i);
    });
  });
});
