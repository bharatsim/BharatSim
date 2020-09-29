import React, { useEffect, useState } from 'react';
import { useParams, useHistory } from 'react-router-dom';
import { Box, Typography } from '@material-ui/core';
import Button from '@material-ui/core/Button';
import { makeStyles } from '@material-ui/core/styles';
import { api } from '../../utils/api';
import LoaderOrError from '../loaderOrError/LoaderOrError';
import useLoader from '../../hook/useLoader';

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
    configureProjectDataBar: {
      height: theme.spacing(16),
      display: 'flex',
      justifyContent: 'space-between',
      backgroundColor: theme.colors.grayScale['100'],
      alignItems: 'center',
      padding: theme.spacing(0, 8),
    },
  };
});

function Project() {
  const [projectMetadata, setProjectMetadata] = useState({
    id: undefined,
    name: 'untitled project',
  });
  const classes = useStyles();
  const { id } = useParams();
  const history = useHistory();
  const { loadingState, startLoader, stopLoaderAfterError, stopLoaderAfterSuccess } = useLoader();

  useEffect(() => {
    async function fetchProjectData() {
      if (id) {
        startLoader();
        try {
          const { project } = await api.getProject(id);
          const { _id, name } = project;
          stopLoaderAfterSuccess();
          setProjectMetadata({ id: _id, name });
        } catch (e) {
          stopLoaderAfterError('failed to load');
        }
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

  function openRecentProjects() {
    history.push('/');
  }

  return (
    <LoaderOrError loadingState={loadingState.state}>
      <Box>
        <Box className={classes.projectNameBar}>
          <Typography variant="h5">{projectMetadata.name}</Typography>
          <Button onClick={saveProject} variant="outlined">
            Save
          </Button>
        </Box>
        <Box className={classes.configureProjectDataBar}>
          <Typography variant="h6"> Configure Dashboard Data</Typography>
          <Button onClick={openRecentProjects}> Back to recent projects</Button>
        </Box>
      </Box>
    </LoaderOrError>
  );
}

export default Project;
