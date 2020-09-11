import React from 'react';
import { render } from '@testing-library/react';
import { Router } from 'react-router-dom';
import { createMemoryHistory } from 'history';

import AppRoute from '../AppRoute';

jest.mock('../component/dashboardLayout/DashboardLayout', () => () => <div>Dashboard Layout</div>);
jest.mock('../component/project/Project', () => () => <div>Project</div>);

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

  it('should navigate to project page ', () => {
    const { container } = renderWithRouter(<AppRoute />, { route: '/project/id' });

    expect(container.innerHTML).toMatch('Project');
  });

  it('should navigate to home page ', () => {
    const { container } = renderWithRouter(<AppRoute />, { route: '/' });

    expect(container.innerHTML).toMatch('home');
  });
});
