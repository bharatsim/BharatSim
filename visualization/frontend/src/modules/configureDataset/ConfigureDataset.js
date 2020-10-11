import React, { useContext } from 'react';
import Box from '@material-ui/core/Box';
import { Typography } from '@material-ui/core';
import Button from '@material-ui/core/Button';
import { useHistory, Link as RouterLink } from 'react-router-dom';
import Link from '@material-ui/core/Link';
import { projectLayoutContext } from '../../contexts/projectLayoutContext';
import useConfigureDatasetStyles from './configureDatasetCSS';

function ConfigureDataset() {
  const classes = useConfigureDatasetStyles();
  const history = useHistory();

  const { projectMetadata, selectedDashboardMetadata } = useContext(projectLayoutContext);

  function openRecentProjects() {
    history.push('/');
  }

  const uploadFilePage = `/projects/${projectMetadata.id}/upload-dataset`;

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
          <Typography variant="subtitle2">{`${projectMetadata.name} :: ${selectedDashboardMetadata.name}`}</Typography>
        </Box>
        <Box className={classes.dashboardDataBody}>
          <Typography variant="subtitle2" color="textPrimary">
            Before we can create any visualization, we ‘ll need some data.
          </Typography>
          <Typography variant="body2">
            Use{' '}
            <Link to={uploadFilePage} component={RouterLink}>
              {' '}
              Upload dataset{' '}
            </Link>
            to add data files to your dashboard.
          </Typography>
        </Box>
      </Box>
    </Box>
  );
}

export default ConfigureDataset;
