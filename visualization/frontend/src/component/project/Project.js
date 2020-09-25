import React, { useEffect, useState } from 'react';

import { useParams, useHistory } from 'react-router-dom';
import { Box } from '@material-ui/core';
import { api } from '../../utils/api';
import TextButton from '../../uiComponent/TextButton';

function Project() {
  const [projectMetadata, setProjectMetadata] = useState({ id: undefined, name: undefined });
  const { id } = useParams();
  const history = useHistory();

  useEffect(() => {
    async function createNewProject() {
      if (id === 'createNew') {
        const { projectId } = await api.createNewProject();
        setProjectMetadata({ id: projectId, name: 'untitled project' });
      }
    }

    createNewProject();
  }, [id]);

  function openRecentProjects() {
    history.push('/');
  }
  return (
    <Box>
      <Box>{projectMetadata.name}</Box>
      <Box>
        <div>Configure Dashboard Data</div>
        <TextButton onClick={openRecentProjects}>Back to recent projects</TextButton>
      </Box>
    </Box>
  );
}

export default Project;
