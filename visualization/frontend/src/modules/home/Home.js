import React from 'react';
import { Box } from '@material-ui/core';
import { makeStyles } from '@material-ui/core/styles';
import ExistingUserHomeScreen from './ExistingUserHomeScreen';
import useFetch from '../../hook/useFetch';
import { api } from '../../utils/api';
import NewUserHomeScreen from './NewUserHomeScreen';
import LoaderOrError from '../../component/loaderOrError/LoaderOrError';

const styles = makeStyles(() => ({
  mainContainer: {
    width: '100%',
    margin: 'auto',
    marginTop: 0,
  },
}));
function Home() {
  const { data: recentProjects, loadingState } = useFetch(api.getProjects);
  const classes = styles();
  return (
    <LoaderOrError loadingState={loadingState}>
      <Box px={32} pt={16} className={classes.mainContainer}>
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
