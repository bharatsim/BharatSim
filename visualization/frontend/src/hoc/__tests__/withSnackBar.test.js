import React, { useEffect } from 'react';
import { useSnackbar } from 'notistack';
import { render } from '@testing-library/react';
import withSnackBar from '../withSnackBar';
import withThemeProvider from '../../theme/withThemeProvider';

function DummyComponent() {
  const { enqueueSnackbar } = useSnackbar();
  useEffect(() => {
    enqueueSnackbar('test snack bar');
  });
  return <div>DummyController </div>;
}

describe('withSnackBar', () => {
  const Component = withThemeProvider(withSnackBar(DummyComponent));
  it('should provide snackbar context to child component', () => {
    const {container} = render(<Component />)
    expect(container).toMatchSnapshot()
  });
});