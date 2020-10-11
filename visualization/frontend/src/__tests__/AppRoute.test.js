import React from 'react';
import { render } from '@testing-library/react';
import { Router } from 'react-router-dom';
import { createMemoryHistory } from 'history';

import AppRoute from '../AppRoute';

jest.mock('../component/dashboardLayout/DashboardLayout', () => () => <div>Dashboard Layout</div>);
jest.mock('../modules/configureDataset/ConfigureDashboardData', () => () => <div>Project</div>);
jest.mock('../modules/home/Home', () => () => <div>Home</div>);
jest.mock('../modules/uploadDataset/UploadDataset', () => () => <div>Upload Dataset</div>);
jest.mock('../modules/layout/projectLayout/projectLayout/ProjectLayout', () => ({ children }) => (
  <div>
    <span>Project layout </span>
    {children}
  </div>
));

function renderWithRouter(
  ui,
  { route = '/', history = createMemoryHistory({ initialEntries: [route] }) } = {},
) {
  return {
    ...render(<Router history={history}>{ui}</Router>),
    history,
  };
}

describe('<AppRoute />', () => {
  it('should navigate to old dashboard layout "/" ', () => {
    const { container } = renderWithRouter(<AppRoute />, { route: '/old-dashboard' });

    expect(container.innerHTML).toMatch('Dashboard Layout');
  });

  it('should navigate to project page /projects/id', () => {
    const { container } = renderWithRouter(<AppRoute />, { route: '/projects/id' });

    expect(container.innerHTML).toMatch('Project');
  });

  it('should navigate to home page ', () => {
    const { container } = renderWithRouter(<AppRoute />, { route: '/' });

    expect(container.innerHTML).toMatch('Home');
  });

  it('should navigate to uoload dataset page ', () => {
    const { container } = renderWithRouter(<AppRoute />, { route: '/projects/id/upload-dataset' });

    expect(container.innerHTML).toMatch('Upload Dataset');
  });
});
