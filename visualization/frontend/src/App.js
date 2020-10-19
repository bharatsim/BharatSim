import React from 'react';
import { makeStyles } from '@material-ui/core/styles';

import AppRoute from './AppRoute';
import withThemeProvider from './theme/withThemeProvider';
import withAppLayout from './modules/layout/appLayout/withAppLayout';
import withSnackBar from './hoc/withSnackBar';

const useRootStyles = makeStyles(() => ({
  root: {
    minHeight: 'calc(100vh - 64px)',
    display: 'flex',
    flex: 1,
    flexDirection: 'column',
  },
}));


function App() {
  const classes = useRootStyles();

  return (
    <div className={classes.root}>
      <AppRoute />
    </div>
  );
}

export default withThemeProvider(withSnackBar(withAppLayout(App)));
