import React from 'react';
import { render, waitFor } from '@testing-library/react';

import useFetch from '../../../hook/useFetch';
import DatasourceSelector from '../DatasourceSelector';

jest.mock('../../../hook/useFetch');

describe('<DatasourceSelector />', () => {
  beforeEach(() => {
    useFetch.mockReturnValue({
      data: {
        dataSources: [
          { name: 'a', _id: 'id_1' },
          { name: 'b', _id: 'id_2' },
        ],
      },
      loadingState: 'SUCCESS',
    });
  });

  it('should match snapshot', () => {
    const { container } = render(
      <DatasourceSelector error="" handleDataSourceChange={jest.fn()} value="" />,
    );

    expect(container).toMatchSnapshot();
  });

  it('should match snapshot for loading state', () => {
    useFetch.mockReturnValue({
      data: undefined,
      loadingState: 'LOADING',
    });

    const { container } = render(
      <DatasourceSelector error="" handleDataSourceChange={jest.fn()} value="" />,
    );

    expect(container).toMatchSnapshot();
  });

  it('should match snapshot for Error state', () => {
    useFetch.mockReturnValue({
      data: undefined,
      loadingState: 'ERROR',
    });

    const { container } = render(
      <DatasourceSelector error="" handleDataSourceChange={jest.fn()} value="" />,
    );

    expect(container).toMatchSnapshot();
  });

  it('should display message of no data source preset if api return empty datasource array', async () => {
    useFetch.mockReturnValue({
      data: { dataSources: [] },
      loadingState: 'SUCCESS',
    });

    const { queryByText } = render(
      <DatasourceSelector error="" handleDataSourceChange={jest.fn()} value="" />,
    );

    await waitFor(() => {
      expect(queryByText(/No data source present, upload data source/i)).not.toBeNull();
    });
  });
});
