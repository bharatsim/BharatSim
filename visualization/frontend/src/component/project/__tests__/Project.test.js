import React from 'react';
import { render } from '@testing-library/react';
import Project from '../Project';

describe('Project', () => {
  it('should match snapshot', () => {
    const { container } = render(<Project />);

    expect(container).toMatchSnapshot();
  });
});
