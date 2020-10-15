import React from 'react';
import { Box, Typography } from '@material-ui/core';

import { makeStyles } from '@material-ui/core/styles';
import { ChildrenPropTypes } from '../commanPropTypes';

const useProjectLayoutStyle = makeStyles((theme) => {
  return {
    projectNameBar: {
      textTransform: 'capitalize',
      boxShadow: '0px 1px 1px rgba(78, 96, 176, 0.3)',
      height: theme.spacing(12),
      padding: theme.spacing(0, 8),
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
    },
  };
});

function ProjectHeader({ children }) {
  const classes = useProjectLayoutStyle();

  return (
    <Box className={classes.projectNameBar}>
      <Typography variant="h5">{children}</Typography>
    </Box>
  );
}

ProjectHeader.propTypes = {
  children: ChildrenPropTypes.isRequired,
};

export default ProjectHeader;
