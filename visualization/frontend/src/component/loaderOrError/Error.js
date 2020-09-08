import React from 'react';

import { Box } from '@material-ui/core';
import { makeStyles } from '@material-ui/core/styles';
import Alert from '@material-ui/lab/Alert';

const styles = makeStyles(() => ({
  root: {
    display: 'flex',
    flex: 1,
    alignItems: 'center',
    height: '100%',
    minHeight: '100px',
    justifyContent: 'center',
  },
}));

function Error() {
  const classes = styles();
  return (
    <Box className={classes.root}>
      <Alert severity="error">Failed to load, Refresh the page</Alert>
    </Box>
  );
}

export default Error;
