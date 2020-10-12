import React from 'react';
import { Box, Typography } from '@material-ui/core';
import { makeStyles } from '@material-ui/core/styles';
import ClickableCard from '../../uiComponent/ClickableCard';
import useModal from '../../hook/useModal';
import Modal from '../../uiComponent/Modal';

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
  const { openModal, isOpen, closeModal } = useModal();

  return (
    <>
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
      <Modal open={isOpen} title="New Dashboard" handleClose={closeModal}>
        Create Project
      </Modal>
    </>
  );
}

export default ProjectHomeScreen;
