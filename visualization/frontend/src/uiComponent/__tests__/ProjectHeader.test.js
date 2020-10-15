import React from 'react';
import { render } from '@testing-library/react';
import ProjectHeader from '../ProjectHeader';

describe('<ProjectHeader />', () => {
  it('should match snapshot', () => {
    const { container } = render(<ProjectHeader>Title</ProjectHeader>);

    expect(container).toMatchSnapshot();
  });
});
