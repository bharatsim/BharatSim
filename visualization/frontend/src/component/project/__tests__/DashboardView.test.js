import React from 'react';
import { render } from '@testing-library/react';
import DashboardView from '../DashboardView';

const views = [{ name: 'dashbaord1' }, { name: 'dashbaord2' }];
describe('DashboardView', () => {
  it('should match snapshot for provided value', function () {
    const { container } = render(<DashboardView value={1} views={views} />);
    expect(container).toMatchSnapshot();
  });
});
