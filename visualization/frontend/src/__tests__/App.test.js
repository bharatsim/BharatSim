import React from 'react';
import { act, render } from '@testing-library/react';
import App from '../App';
import { initApiConfig } from '../utils/fetch';

jest.mock('../utils/fetch');

jest.mock('../component/dashboardLayout/DashboardLayout', () => () => <>Dash Board Layout</>);

let mockShouldShowError;

describe('<App />', () => {
  beforeEach(() => {
    initApiConfig.mockImplementation(({ setError }) => {
      mockShouldShowError = setError;
    });
  });
  it('should match snapshot', () => {
    const { container } = render(<App />);

    expect(container).toMatchSnapshot();
  });
  it('should show error with reload button if api is failing', () => {
    const { queryByText } = render(<App />);

    act(() => {
      mockShouldShowError(true);
    });
    const errorComponent = queryByText('Error occurred while loading the page');

    expect(errorComponent).toBeInTheDocument();
  });
  it('should reload the page on click of reload button of error component', () => {
    const reloadFunction = jest.fn();
    const { location } = window;
    delete window.location;
    window.location = { reload: reloadFunction };
    const { getByText } = render(<App />);

    act(() => {
      mockShouldShowError(true);
    });

    const reloadButton = getByText('Reload');
    reloadButton.click();

    expect(reloadFunction).toHaveBeenCalled();
    window.location = location;
  });
});
