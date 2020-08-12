import React from 'react';

import { fireEvent, render, waitFor, within } from '@testing-library/react';

import ChartConfigModal from '../ChartConfigModal';
import { fetch } from '../../../utils/fetch';
import { chartTypes } from '../../../constants/charts';
import { selectDropDownOption } from '../../../testUtil';

jest.mock('../../../utils/url', () => ({
  url: {
    getHeaderUrl: jest.fn().mockReturnValue('/headers'),
    DATA_SOURCES: '/datasources',
  },
}));

jest.mock('../../../utils/fetch', () => ({
  fetch: jest.fn(({ url }) => {
    if (url === '/headers') {
      return Promise.resolve({ headers: ['x-header', 'y-header'] });
    }
    if (url === '/datasources') {
      return Promise.resolve({
        dataSources: [
          { _id: 'id1', name: 'modelone' },
          { _id: 'id2', name: 'modeltwo' },
        ],
      });
    }
    return Promise.reject();
  }),
}));

describe('<ChartConfigModal />', () => {
  const props = {
    onCancel: jest.fn(),
    onOk: jest.fn(),
    open: true,
    chartType: chartTypes.LINE_CHART,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should match a snapshot for <ChartConfigModal />', async () => {
    render(<ChartConfigModal {...props} />);

    await waitFor(() => document.querySelector('.MuiPaper-root'));

    expect(document.querySelector('.MuiPaper-root')).toMatchSnapshot();
  });

  it('should be show x-axis and y-axis dropdown if data source selected', async () => {
    render(<ChartConfigModal {...props} />);

    await waitFor(() => document.querySelector('.MuiPaper-root'));

    const configModal = within(document.querySelector('.MuiPaper-root'));

    selectDropDownOption(configModal, 'dropdown-dataSources', 'id1');

    await waitFor(() => configModal.queryByText('dropdown-x'));

    const xDropdown = within(configModal.getByTestId('dropdown-x'));
    const yDropdown = within(configModal.getByTestId('dropdown-y'));

    await waitFor(() => {
      expect(xDropdown.getByRole('button')).toBeInTheDocument();
      expect(yDropdown.getByRole('button')).toBeInTheDocument();
    });
  });

  it('should be hide x-axis and y-axis dropdown if data source is not selected', async () => {
    render(<ChartConfigModal {...props} />);

    await waitFor(() => document.querySelector('.MuiPaper-root'));

    const configModal = within(document.querySelector('.MuiPaper-root'));

    const xDropdown = configModal.queryByTestId('dropdown-x');
    const yDropdown = configModal.queryByTestId('dropdown-y');

    expect(xDropdown).toBeNull();
    expect(yDropdown).toBeNull();
  });

  it('should pass selected chart configs to onOK callback', async () => {
    render(<ChartConfigModal {...props} />);

    await waitFor(() => document.querySelector('.MuiPaper-root'));

    const configModal = within(document.querySelector('.MuiPaper-root'));

    selectDropDownOption(configModal, 'dropdown-dataSources', 'id1');

    await waitFor(() => expect(fetch).toBeCalledTimes(2));

    selectDropDownOption(configModal, 'dropdown-x', 'x-header');

    selectDropDownOption(configModal, 'dropdown-y', 'y-header');

    const okButton = configModal.getByText(/Ok/i);

    fireEvent.click(okButton);

    expect(props.onOk).toHaveBeenCalledWith({
      dataSource: 'id1',
      xAxis: 'x-header',
      yAxis: 'y-header',
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
      expect(document.querySelector('.MuiPaper-root')).toHaveTextContent(
        /No data source present, upload data source/i,
      );
    });
  });
});
