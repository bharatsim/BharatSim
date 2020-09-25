import React from 'react';
import { Box } from '@material-ui/core';
import { useHistory } from 'react-router-dom';
import ExistingUserHomeScreen from './ExistingUserHomeScreen';

function Home() {
  const history = useHistory();

  function createNewProject() {
    history.push('/project/createNew');
  }

  return (
    <Box px={32}>
      <ExistingUserHomeScreen />
    </Box>
  );
}

export default Home;
