import React from 'react';
import { SnackbarProvider } from 'notistack';
import { makeStyles } from '@material-ui/core/styles';
import failureIcon from '../assets/images/error-icon.svg';
import successIcon from '../assets/images/success-icon.svg';

const useSnackBarStyles = makeStyles((theme) => ({
  contentRoot: {
    padding: theme.spacing(1, 4),
  },
  message: {
    padding: 0,
  },
  variantSuccess: {
    backgroundColor: `#EAF4E5 !important`,
    borderColor: `${theme.palette.success.main}33`,
    border: '1px solid',
    color: `${theme.palette.success.dark} !important`,
    ...theme.typography.body2,
    borderRadius: theme.spacing(1),
    boxShadow: 'unset',
    opacity: 1,
    minWidth: theme.spacing(120),
  },
  variantError: {
    backgroundColor: `#FBE9E4 !important`,
    borderColor: `${theme.palette.error.light}33`,
    border: '1px solid',
    color: `${theme.palette.error.dark} !important`,
    ...theme.typography.body2,
    borderRadius: theme.spacing(1),
    boxShadow: 'unset',
    opacity: 1,
    minWidth: theme.spacing(120),
  },
}));

const useIconStyles = makeStyles((theme) => ({
  iconRoot: {
    marginRight: theme.spacing(2),
    height: theme.spacing(10),
    width: theme.spacing(10),
  },
}));

function withSnackBar(WrappedComponent) {
  return (props) => {
    const snackBarClasses = useSnackBarStyles();
    const iconClasses = useIconStyles();
    return (
      <SnackbarProvider
        maxSnack={3}
        anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
        classes={snackBarClasses}
        iconVariant={{
          error: <img src={failureIcon} alt="error" className={iconClasses.iconRoot} />,
          success: <img src={successIcon} alt="success" className={iconClasses.iconRoot} />,
        }}
      >
        <WrappedComponent {...props} />
      </SnackbarProvider>
    );
  };
}

export default withSnackBar;
