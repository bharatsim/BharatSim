import React from 'react';
import { render } from '@testing-library/react';
import SideBarLayout from '../SideBarLayout';

function DummyController() {
  return <div>DummyController</div>;
}

function DummyView() {
  return <div>DummyView</div>;
}

describe('<SideBarLayout />', () => {
  it('should mactch snapshot', () => {
    const { container } = render(
      <SideBarLayout ViewComponent={DummyView} ControllerComponent={DummyController} />,
    );

    expect(container).toMatchSnapshot();
  });
});
