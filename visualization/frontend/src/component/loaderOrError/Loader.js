import React from 'react';

import { CircularProgress, Box } from '@material-ui/core';
import { makeStyles } from '@material-ui/core/styles';

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

function Loader() {
  const classes = styles();
  return (
    <Box className={classes.root}>
      <CircularProgress />
    </Box>
  );
}

export default Loader;
