import React from 'react';
import { Box } from '@material-ui/core';

import styles from './headerCSS';

import bharatSimLogo from '../../../assets/images/PlaceholderLogo.svg';

function Header() {
  const classes = styles();
  return (
    <Box className={classes.mainContainer} component="header">
      <Box className={classes.logo}>
        <img src={bharatSimLogo} alt="logo" />
      </Box>
    </Box>
  );
}

export default Header;
