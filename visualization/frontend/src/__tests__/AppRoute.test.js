import React from 'react';
import { render } from '@testing-library/react';
import { Router } from 'react-router-dom';
import { createMemoryHistory } from 'history';

import AppRoute from '../AppRoute';

jest.mock('../component/dashboardLayout/DashboardLayout', () => () => <div>Dashboard Layout</div>);
jest.mock('../modules/configureDataset/ConfigureDataset', () => () => <div>Project configure dataset</div>);
jest.mock('../modules/home/Home', () => () => <div>Home</div>);
jest.mock('../modules/uploadDataset/UploadDataset', () => () => <div>Upload Dataset</div>);
jest.mock('../modules/projectHomeScreen/ProjectHomeScreen', () => () => <div>Project home screen</div>);
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
    const { queryByText } = renderWithRouter(<AppRoute />, { route: '/old-dashboard' });

    expect(queryByText('Dashboard Layout')).not.toBeNull();
  });

  it('should navigate to project page /projects/id/configure-dataset', () => {
    const { queryByText } = renderWithRouter(<AppRoute />, { route: '/projects/id/configure-dataset' });

    expect(queryByText('Project configure dataset')).not.toBeNull();
  });

  it('should navigate to home page', () => {
    const { queryByText } = renderWithRouter(<AppRoute />, { route: '/' });

    expect(queryByText('Home')).not.toBeNull();
  });

  it('should navigate to upload dataset page ', () => {
    const { queryByText } = renderWithRouter(<AppRoute />, { route: '/projects/id/upload-dataset' });

    expect(queryByText('Upload Dataset')).not.toBeNull();
  });

  it('should navigate to project home screen ', () => {
    const { queryByText } = renderWithRouter(<AppRoute />, { route: '/projects/create' });

    expect(queryByText('Project home screen')).not.toBeNull();
  });

  it('should navigate to project home screen with project id', () => {
    const { queryByText } = renderWithRouter(<AppRoute />, { route: '/projects/:id/create-dashboard' });

    expect(queryByText('Project home screen')).not.toBeNull();
  });
});
