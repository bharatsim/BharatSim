import React from 'react';
import { Box } from '@material-ui/core';

import { useHistory } from 'react-router-dom';
import styles from './headerCSS';

import bharatSimLogo from '../../../../assets/images/PlaceholderLogo.svg';

function Header() {
  const classes = styles();
  const history = useHistory();
  function openLandingPage() {
    history.push('/');
  }
  return (
    <Box className={classes.mainContainer} component="header" cursor="pointer">
      <Box className={classes.logo} onClick={openLandingPage}>
        <img src={bharatSimLogo} alt="logo" />
      </Box>
    </Box>
  );
}

export default Header;
