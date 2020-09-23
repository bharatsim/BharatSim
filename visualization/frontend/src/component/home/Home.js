import React from 'react';
import { Box, Typography } from '@material-ui/core';
import Button from '@material-ui/core/Button';
import { makeStyles } from '@material-ui/core/styles';
import { useHistory } from 'react-router-dom';
import bharatSimWelcome from '../../assets/images/welcomePlaceholder.svg';
import ButtonGroup from '../../uiComponent/ButtonGroup';

const styles = makeStyles((theme) => ({
  mainContainer: {
    margin: 'auto',
    marginTop: theme.spacing(21),
  },
  centered: {
    textAlign: 'center',
  },
  introTextContainer: {
    margin: 'auto',
    marginTop: theme.spacing(9),
    width: theme.spacing(144),
  },
  buttonContainer: {
    display: 'flex',
    justifyContent: 'center',
  },
}));

function Home() {
  const classes = styles();
  const history = useHistory();

  function createNewProject() {
    history.push('/project/createNewProject');
  }
  return (
    <Box className={classes.mainContainer}>
      <Typography variant="h4" className={classes.centered}>
        Welcome to BharatSim
      </Typography>
      <Box mt={14}>
        <img src={bharatSimWelcome} alt="logo" />
      </Box>
      <Box className={classes.introTextContainer}>
        <Typography variant="subtitle2" className={classes.centered}>
          Intro text
        </Typography>
        <Typography variant="body2" className={classes.centered}>
          Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt
          ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation
          ullamco
        </Typography>
      </Box>
      <Box mt={5} className={classes.buttonContainer}>
        <ButtonGroup>
          <Button variant="contained" size="large" onClick={createNewProject}>
            Create new Project
          </Button>
        </ButtonGroup>
      </Box>
    </Box>
  );
}

export default Home;
