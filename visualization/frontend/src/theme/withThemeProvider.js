import React from 'react';
import { MuiThemeProvider, CssBaseline } from '@material-ui/core';

import theme from './theme';

function withThemeProvider(WrappedComponent) {
  return function WithTheme(props) {
    return (
      <MuiThemeProvider theme={theme}>
        <CssBaseline />
        <WrappedComponent {...props} />
      </MuiThemeProvider>
    );
  };
}

export default withThemeProvider;
