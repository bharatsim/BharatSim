import React from 'react';
import Box from '@material-ui/core/Box';
import { Typography } from '@material-ui/core';
import DashboardLayout from './component/dashboardLayout/DashboardLayout';

function App() {
  return (
    <>
      <Box pt={2} pb={2}>
        <Typography variant="h3" align="center">
          Welcome to BharatSim Visualization
        </Typography>
        <DashboardLayout />
      </Box>
    </>
  );
}

export default App;
