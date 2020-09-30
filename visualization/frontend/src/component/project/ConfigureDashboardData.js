import React from 'react';
import Box from '@material-ui/core/Box';
import { Typography } from '@material-ui/core';
import Button from '@material-ui/core/Button';
import { makeStyles } from '@material-ui/core/styles';
import { useHistory } from 'react-router-dom';

const useStyles = makeStyles((theme) => {
  return {
    configureProjectDataBar: {
      height: theme.spacing(16),
      width: '100%',
      display: 'flex',
      justifyContent: 'space-between',
      backgroundColor: theme.colors.grayScale['100'],
      alignItems: 'center',
      padding: theme.spacing(0, 8),
    },
    dashboardDataHeader: {
      display: 'flex',
      alignItems: 'center',
      height: theme.spacing(8),
      paddingLeft: theme.spacing(4),
      paddingRight: theme.spacing(4),
      borderTopRightRadius: theme.spacing(1),
      borderTopLeftRadius: theme.spacing(1),
      backgroundColor: theme.colors.primaryColorScale['500'],
      boxShadow: 'inset 0px -1px 0px rgba(0, 0, 0, 0.12)',
      color: theme.colors.textLight.primary,
      textTransform: 'capitalize',
    },
    dashboardDataContainer: {
      width: theme.spacing(256),
      margin: theme.spacing(8, 8, 16, 8),
    },
    dashboardDataBody: {
      display: 'flex',
      flexDirection: 'column',
      justifyContent: 'center',
      alignItems: 'center',
      boxSizing: 'border-box',
      boxShadow: 'inset 0px -1px 0px rgba(0, 0, 0, 0.12)',
      height: theme.spacing(22),
      border: '1px solid rgba(0, 0, 0, 0.2)',
    },
  };
});
export default function ConfigureDashboardData({ dashboardData, projectName }) {
  const classes = useStyles();
  const history = useHistory();
  function openRecentProjects() {
    history.push('/');
  }
  return (
    <Box>
      <Box className={classes.configureProjectDataBar}>
        <Typography variant="h6"> Configure Dashboard Data</Typography>
        <Button onClick={openRecentProjects}> Back to recent projects</Button>
      </Box>
      <Box className={classes.dashboardDataContainer}>
        <Box>
          <Typography variant="subtitle2"> Manage Dashboard Dataset</Typography>
        </Box>
        <Box className={classes.dashboardDataHeader}>
          <Typography variant="subtitle2">{`${projectName} :: ${dashboardData.name}`}</Typography>
        </Box>
        <Box className={classes.dashboardDataBody}>
          <Typography variant="subtitle2" color="textPrimary">
            Before we can create any visualization, we ‘ll need some data.
          </Typography>
          <Typography variant="body2">
            Use Upload dataset to add data files to your dashboard.
          </Typography>
        </Box>
      </Box>
    </Box>
  );
}
