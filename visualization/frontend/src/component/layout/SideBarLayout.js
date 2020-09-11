import React from 'react';
import PropTypes from 'prop-types';
import { Box, Typography } from '@material-ui/core';

import styles from './sideBarLayoutCSS';

import bharatSimLogo from '../../assets/images/Logobharatsim-logo.svg';

import labels from './sidebarLayout.labels';

function SideBarLayout({ ControllerComponent, ViewComponent }) {
  const classes = styles();

  return (
    <Box className={classes.main}>
      <Box className={classes.sideBar}>
        <Box className={classes.logo}>
          <Box pr={5}>
            <img src={bharatSimLogo} alt="logo" />
          </Box>
          <Typography variant="h1" className={classes.logoText}>
            {labels.LOGO_TEXT}
          </Typography>
        </Box>
        <Box>
          <ControllerComponent />
        </Box>
      </Box>
      <Box className={classes.view}>
        <ViewComponent />
      </Box>
    </Box>
  );
}

SideBarLayout.propTypes = {
  ControllerComponent: PropTypes.elementType.isRequired,
  ViewComponent: PropTypes.elementType.isRequired,
};

export default SideBarLayout;
