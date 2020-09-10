import React from 'react';
import { MuiThemeProvider, CssBaseline } from '@material-ui/core';

import theme from './theme';

function withThemeProvider(WrappedComponent) {
  return function WithTheme() {
    return (
      <MuiThemeProvider theme={theme}>
        <CssBaseline />
        <WrappedComponent />
      </MuiThemeProvider>
    );
  };
}

export default withThemeProvider;
