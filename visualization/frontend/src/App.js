import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import { makeStyles } from '@material-ui/core/styles';

import AppRoute from './AppRoute';
import withThemeProvider from './theme/withThemeProvider';

const styles = makeStyles(() => ({
  root: {
    height: '100vh',
    display: 'flex',
    flex: 1,
    flexDirection: 'column',
  },
}));

function App() {
  const classes = styles();
  return (
    <Router>
      <div className={classes.root}>
        <AppRoute />
      </div>
    </Router>
  );
}

export default withThemeProvider(App);
