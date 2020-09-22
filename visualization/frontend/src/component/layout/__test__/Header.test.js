import React from 'react';
import { render } from '@testing-library/react';
import withHeader from '../withHeader';
import withThemeProvider from '../../../theme/withThemeProvider';

function DummyController() {
  return <div>DummyController</div>;
}

const Component = withThemeProvider(withHeader(DummyController));

describe('<Header />', () => {
  it('should match snapshot', () => {
    const { container } = render(<Component />);

    expect(container).toMatchSnapshot();
  });
});
