import React from 'react';
import { makeStyles } from '@material-ui/core/styles';

import AppRoute from './AppRoute';
import withThemeProvider from './theme/withThemeProvider';
import withAppLayout from './modules/layout/appLayout/withAppLayout';

const styles = makeStyles(() => ({
  root: {
    minHeight: 'calc(100vh - 64px)',
    display: 'flex',
    flex: 1,
    flexDirection: 'column',
  },
}));

function App() {
  const classes = styles();
  return (
    <div className={classes.root}>
      <AppRoute />
    </div>
  );
}

export default withThemeProvider(withAppLayout(App));
