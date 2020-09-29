import React from 'react';
import { Box } from '@material-ui/core';
import ExistingUserHomeScreen from './ExistingUserHomeScreen';
import useFetch from '../../hook/useFetch';
import { api } from '../../utils/api';
import NewUserHomeScreen from './NewUserHomeScreen';
import LoaderOrError from '../loaderOrError/LoaderOrError';

function Home() {
  const { data: recentProjects, loadingState } = useFetch(api.getProjects);
  return (
    <LoaderOrError loadingState={loadingState}>
      <Box px={32} pt={16}>
        {recentProjects && recentProjects.projects.length > 0 ? (
          <ExistingUserHomeScreen recentProjects={recentProjects.projects} />
        ) : (
          <NewUserHomeScreen />
        )}
      </Box>
    </LoaderOrError>
  );
}

export default Home;
