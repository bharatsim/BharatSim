import Box from '@material-ui/core/Box';
import React from 'react';
import Typography from '@material-ui/core/Typography';
import { makeStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';

const useStyles = makeStyles((theme) => {
  return {
    errorTitle: {
      fontWeight: theme.typography.fontWeightBold,
      lineHeight: 1,
      color: theme.palette.error.dark,
    },
    errorMessage: {
      lineHeight: 1,
      color: theme.palette.error.dark,
    },
    hidden: {
      visibility: 'hidden',
      height: theme.spacing(8),
    },
    errorBox: {
      display: 'flex',
      height: theme.spacing(8),
      border: '1px solid',
      borderRadius: theme.spacing(1),
      borderColor: '#FFC5B3',
      backgroundColor: '#FFEEE8',
      padding: theme.spacing(2, 6),
    },
  };
});
function ErrorBar({ visible, message }) {
  const classes = useStyles();
  return (
    <Box className={visible ? classes.errorBox : classes.hidden}>
      <Typography variant="body2" color="error" classes={{ body2: classes.errorTitle }}>
        Error: &nbsp;
      </Typography>

      <Typography variant="body2" color="error" classes={{ body2: classes.errorMessage }}>
        {message}
      </Typography>
    </Box>
  );
}

ErrorBar.propTypes = {
  visible: PropTypes.bool,
  message: PropTypes.string,
};
ErrorBar.defaultProps = {
  visible: false,
  message: '',
};
export default ErrorBar;
