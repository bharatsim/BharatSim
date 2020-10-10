import React, { useEffect, useState } from 'react';

import { useParams, useHistory } from 'react-router-dom';
import { Box, Typography } from '@material-ui/core';
import Button from '@material-ui/core/Button';
import { makeStyles } from '@material-ui/core/styles';
import { api } from '../../../utils/api';
import LoaderOrError from '../../../component/loaderOrError/LoaderOrError';
import useLoader from '../../../hook/useLoader';
import DashboardNavbar from './sideBarLayout/DashboardNavbar';

const useStyles = makeStyles((theme) => {
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
    layoutContainer: {
      display: 'flex',
      width: '100%',
      height: '100%',
      minHeight: 'calc(100vh - 64px)',
    },
    sideBarLayout: {
      display: 'flex',
      flexDirection: 'column',
      background: theme.colors.primaryColorScale['600'],
      width: theme.spacing(64),
      color: theme.colors.textLight.primary,
      paddingTop: theme.spacing(18),
    },
  };
});
export default function withProjectLayout(WrappedComponent) {
  return function ProjectLayout(props) {
    const [projectMetadata, setProjectMetadata] = useState({
      id: undefined,
      name: 'untitled project',
    });
    const dashboards = [{ name: 'dashboard1' }];
    const [selectedDashboard] = useState(0);
    const classes = useStyles();
    const history = useHistory();
    const { id } = useParams();
    const { loadingState, startLoader, stopLoaderAfterError, stopLoaderAfterSuccess } = useLoader();
    useEffect(() => {
      startLoader();

      async function fetchProjectData() {
        if (id) {
          try {
            const { project } = await api.getProject(id);
            const { _id, name } = project;
            stopLoaderAfterSuccess();
            setProjectMetadata({ id: _id, name });
          } catch (e) {
            stopLoaderAfterError('failed to load');
          }
        } else {
          stopLoaderAfterSuccess();
        }
      }

      fetchProjectData();
    }, [id]);

    async function saveProject() {
      const { projectId } = await api.saveProject(projectMetadata);
      if (!projectMetadata.id) {
        history.replace({ pathname: `/projects/${projectId}` });
      }
    }

    return (
      <LoaderOrError loadingState={loadingState.state}>
        <Box className={classes.layoutContainer}>
          <Box className={classes.sideBarLayout}>
            <DashboardNavbar navItems={['dashboard1']} value={selectedDashboard} />
          </Box>
          <Box display="flex" flex={1} flexDirection="column">
            <Box className={classes.projectNameBar}>
              <Typography variant="h5">{projectMetadata.name}</Typography>
              <Button onClick={saveProject} variant="outlined">
                Save
              </Button>
            </Box>
            <WrappedComponent
              {...props}
              dashboardData={dashboards[selectedDashboard]}
              projectMetadata={projectMetadata}
            />
          </Box>
        </Box>
      </LoaderOrError>
    );
  };
}
