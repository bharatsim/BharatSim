import React, { useContext } from 'react';
import { useHistory } from 'react-router-dom';
import { Box, Typography } from '@material-ui/core';
import { makeStyles } from '@material-ui/core/styles';
import ClickableCard from '../../uiComponent/ClickableCard';
import useModal from '../../hook/useModal';
import CreateNewDashboardModal from './CreateNewDashboardModal';
import LoaderOrError from '../../component/loaderOrError/LoaderOrError';
import useFetchExecutor from '../../hook/useFetchExecuter';
import { api } from '../../utils/api';
import ProjectHeader from '../../uiComponent/ProjectHeader';
import { projectLayoutContext } from '../../contexts/projectLayoutContext';

const useStyles = makeStyles((theme) => {
  return {
    addProjectContainer: {
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      flexDirection: 'column',
      height: theme.spacing(66),
      boxSizing: 'border-box',
    },
  };
});

function ProjectHomeScreen() {
  const classes = useStyles();
  const history = useHistory();
  const { executeFetch, loadingState } = useFetchExecutor();
  const { openModal, isOpen, closeModal } = useModal();
  const { projectMetadata } = useContext(projectLayoutContext);

  async function onCreate(values) {
    closeModal();
    const { projectId } = await executeFetch(api.saveProject, [{ name: values['project-title'] }]);
    await executeFetch(api.addNewDashboard, [{ name: values['dashboard-title'], projectId }]);
    history.push(`/projects/${projectId}`);
  }

  return (
    <LoaderOrError loadingState={loadingState}>
      <Box>
        <ProjectHeader>{projectMetadata.name}</ProjectHeader>
        <Box py={14} px={32}>
          <ClickableCard onClick={openModal}>
            <Box className={classes.addProjectContainer}>
              <Box pb={2}>
                <Typography variant="h6"> You don’t have any dashboards Yet. </Typography>
              </Box>
              <Typography variant="body2"> Click here to create your first dashboard. </Typography>
            </Box>
          </ClickableCard>
        </Box>
        <CreateNewDashboardModal isOpen={isOpen} closeModal={closeModal} onCreate={onCreate} />
      </Box>
    </LoaderOrError>
  );
}

export default ProjectHomeScreen;
