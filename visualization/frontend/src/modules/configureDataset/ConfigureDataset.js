import React, { useContext, useEffect, useState } from 'react';
import Box from '@material-ui/core/Box';
import { Typography } from '@material-ui/core';
import Button from '@material-ui/core/Button';
import { Link as RouterLink, useHistory } from 'react-router-dom';
import Link from '@material-ui/core/Link';
import { projectLayoutContext } from '../../contexts/projectLayoutContext';
import useConfigureDatasetStyles from './configureDatasetCSS';
import ProjectHeader from '../../uiComponent/ProjectHeader';
import useFetch from '../../hook/useFetch';
import { api } from '../../utils/api';
import LoaderOrError from '../../component/loaderOrError/LoaderOrError';
import DashboardDataSetsTable from './DashboardDataSetsTable';
import plusIcon from '../../assets/images/plus.svg';

function ConfigureDataset() {
  const classes = useConfigureDatasetStyles();
  const history = useHistory();
  const [dataSources, setDataSources] = useState();

  const {
    projectMetadata,
    selectedDashboardMetadata: { _id: selectedDashboardId, name: selectedDashboardName },
  } = useContext(projectLayoutContext);

  const { data: fetchedDataSources, loadingState } = useFetch(api.getDatasources, [
    selectedDashboardId,
  ]);

  const uploadFilePage = `/projects/${projectMetadata.id}/upload-dataset`;

  useEffect(() => {
    if (fetchedDataSources) {
      setDataSources(fetchedDataSources.dataSources);
    }
  }, [fetchedDataSources]);

  function openRecentProjects() {
    history.push('/');
  }

  function openUploadDatasets() {
    history.push(uploadFilePage);
  }

  return (
    <LoaderOrError loadingState={loadingState}>
      <Box>
        <ProjectHeader>{projectMetadata.name}</ProjectHeader>
        <Box className={classes.configureProjectDataBar}>
          <Typography variant="h6"> Configure Dashboard Data</Typography>
          <Button onClick={openRecentProjects} variant="text" size="small">
            Back to recent projects
          </Button>
        </Box>
        <Box className={classes.dashboardDataContainer}>
          <Box className={classes.dashboardDataContainerTitle}>
            <Typography variant="subtitle2"> Manage Dashboard Dataset</Typography>
            <Button
              color="secondary"
              variant="contained"
              size="small"
              startIcon={<img src={plusIcon} alt="icon" />}
              onClick={openUploadDatasets}
            >
              Upload Data
            </Button>
          </Box>
          <Box className={classes.dashboardDataHeader}>
            <Typography variant="subtitle2">{`${projectMetadata.name} :: ${selectedDashboardName}`}</Typography>
          </Box>
          <Box className={classes.dashboardDataBody}>
            {dataSources && dataSources.length > 0 ? (
              <DashboardDataSetsTable dataSources={dataSources} />
            ) : (
              <Box className={classes.noDataSourcesMessage}>
                <Typography variant="subtitle2" color="textPrimary">
                  Before we can create any visualization, we ‘ll need some data.
                </Typography>
                <Typography variant="body2">
                  Use
                  {' '}
                  <Link to={uploadFilePage} component={RouterLink}>
                    {' '}
                    Upload dataset
                    {' '}
                  </Link>
                  to add data files to your dashboard.
                </Typography>
                {' '}
              </Box>
            )}
          </Box>
        </Box>
      </Box>
    </LoaderOrError>
  );
}

export default ConfigureDataset;
