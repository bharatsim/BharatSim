import React from 'react';
import { render } from '@testing-library/react';
import { useTheme } from '@material-ui/core';

import withThemeProvider from '../withThemeProvider';

function DummyChildComponent() {
  const theme = useTheme();
  return (
    <div>
      DummyChildComponent
      <span>{JSON.stringify(theme, null, 2)}</span>
    </div>
  );
}

describe('withThemeProvider HOC', () => {
  it('should match snapshot', () => {
    const Component = withThemeProvider(DummyChildComponent);
    const { container } = render(<Component />);

    expect(container).toMatchSnapshot();
  });
});
