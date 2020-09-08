import React from 'react';
import { Box, Typography } from '@material-ui/core';
import { makeStyles } from '@material-ui/core/styles';

import DashboardLayout from './component/dashboardLayout/DashboardLayout';

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
    <div className={classes.root}>
      <Box pt={2} pb={2}>
        <Typography variant="h3" align="center">
          Welcome to BharatSim Visualization
        </Typography>
      </Box>
      <DashboardLayout />
    </div>
  );
}

export default App;
