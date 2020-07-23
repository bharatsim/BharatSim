import React from 'react';
import { render } from '@testing-library/react';
import App from '../App';

jest.mock('../component/dashboardLayout/DashboardLayout', () => () => <>Dash Board Layout</>);

describe('<App />', () => {
  it('should match snapshot', () => {
    const { container } = render(<App />);

    expect(container).toMatchSnapshot();
  });
});
