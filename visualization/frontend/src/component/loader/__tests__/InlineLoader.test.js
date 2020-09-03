import React from 'react';

import { render } from '@testing-library/react';
import InlineLoader from '../InlineLoader';

describe('<InlineLoader />', () => {
  it('should match snapshot', () => {
    const { container } = render(<InlineLoader status="LOADING" message="Loading..." />);

    expect(container).toMatchSnapshot();
  });
});
