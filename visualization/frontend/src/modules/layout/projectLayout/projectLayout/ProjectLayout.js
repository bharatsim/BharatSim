import { useHistory, useParams } from 'react-router-dom';

import React, { useEffect, useState } from 'react';
import { Box, Typography } from '@material-ui/core';
import Button from '@material-ui/core/Button';
import { api } from '../../../../utils/api';
import LoaderOrError from '../../../../component/loaderOrError/LoaderOrError';
import SideDashboardNavbar from '../sideDashboardNavbar/SideDashboardNavbar';
import useFetch from '../../../../hook/useFetch';
import useProjectLayoutStyle from './projectLayoutCSS';
import { ProjectLayoutProvider } from '../../../../contexts/projectLayoutContext';

async function fetchProjectData(id) {
  if (id) {
    return api.getProject(id);
  }
  return null;
}

export default function ProjectLayout({ children }) {
  const classes = useProjectLayoutStyle();
  const history = useHistory();
  const { id } = useParams();

  const [projectMetadata, setProjectMetadata] = useState({
    id: undefined,
    name: 'untitled project',
  });

  const [selectedDashboard] = useState(0);

  const { data: fetchedProjectMetadata, loadingState } = useFetch(fetchProjectData, [id]);

  const dashboards = [{ name: 'dashboard1' }];

  useEffect(() => {
    if (fetchedProjectMetadata) {
      const { _id, name } = fetchedProjectMetadata.project;
      setProjectMetadata({ id: _id, name });
    }
  }, [fetchedProjectMetadata]);

  async function saveProject() {
    const { projectId } = await api.saveProject(projectMetadata);
    if (!projectMetadata.id) {
      history.replace({ pathname: `/projects/${projectId}` });
    }
  }

  return (
    <LoaderOrError loadingState={loadingState}>
      <Box className={classes.layoutContainer}>
        <Box className={classes.sideBarLayout}>
          <SideDashboardNavbar navItems={['dashboard1']} value={selectedDashboard} />
        </Box>
        <Box display="flex" flex={1} flexDirection="column">
          <Box className={classes.projectNameBar}>
            <Typography variant="h5">{projectMetadata.name}</Typography>
            <Button onClick={saveProject} variant="outlined">
              Save
            </Button>
          </Box>
          <ProjectLayoutProvider
            value={{ projectMetadata, selectedDashboardMetadata: dashboards[selectedDashboard] }}
          >
            {children}
          </ProjectLayoutProvider>
        </Box>
      </Box>
    </LoaderOrError>
  );
}
