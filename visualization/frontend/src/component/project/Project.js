import React, { useEffect, useState } from 'react';
import { useParams, useHistory } from 'react-router-dom';
import { Box, Typography } from '@material-ui/core';
import Button from '@material-ui/core/Button';
import { makeStyles } from '@material-ui/core/styles';
import { api } from '../../utils/api';

const useStyles = makeStyles((theme) => {
  return {
    projectNameBar: {
      textTransform: 'capitalize',
      boxShadow: '0px 1px 1px rgba(78, 96, 176, 0.3)',
      height: theme.spacing(12),
      paddingLeft: theme.spacing(8),
      display: 'flex',
      alignItems: 'center',
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
  const [projectMetadata, setProjectMetadata] = useState({ id: undefined, name: undefined });
  const classes = useStyles();
  const { id } = useParams();
  const history = useHistory();

  useEffect(() => {
    async function createNewProject() {
      if (id === 'createNew') {
        const { projectId } = await api.createNewProject();
        setProjectMetadata({ id: projectId, name: 'untitled project' });
      } else {
        const { projects } = await api.getProject(id);
        const { _id, name } = projects;
        setProjectMetadata({ id: _id, name });
      }
    }

    createNewProject();
  }, [id]);

  function openRecentProjects() {
    history.push('/');
  }

  return (
    <Box>
      <Box className={classes.projectNameBar}>
        <Typography variant="h5">{projectMetadata.name}</Typography>
      </Box>
      <Box className={classes.configureProjectDataBar}>
        <Typography variant="h6"> Configure Dashboard Data</Typography>
        <Button onClick={openRecentProjects}> Back to recent projects</Button>
      </Box>
    </Box>
  );
}

export default Project;
