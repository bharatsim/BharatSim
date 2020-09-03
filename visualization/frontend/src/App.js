import React, { useState } from 'react';
import { Typography, Box, Link } from '@material-ui/core';
import { Alert } from '@material-ui/lab';
import DashboardLayout from './component/dashboardLayout/DashboardLayout';
import { initApiConfig } from './utils/fetch';

function App() {
  const [error, setError] = useState();

  initApiConfig({ setError });
  return (
    <>
      {error && (
        <Alert severity="error">
          <span> Error occurred while loading the page </span>
          <Link onClick={() => window.location.reload()}>Reload</Link>
        </Alert>
      )}

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
