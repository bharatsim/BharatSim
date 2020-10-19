import React from "react";
import { makeStyles } from "@material-ui/core/styles";
import { SnackbarProvider } from "notistack";
import InfoIcon from "@material-ui/icons/Info";

import AppRoute from "./AppRoute";
import withThemeProvider from "./theme/withThemeProvider";
import withAppLayout from "./modules/layout/appLayout/withAppLayout";
import useSnackBarStyles from "./uiComponent/SnackBarCSS";

const useRootStyles = makeStyles((theme) => ({
  root: {
    minHeight: 'calc(100vh - 64px)',
    display: 'flex',
    flex: 1,
    flexDirection: 'column',
  },
  iconRoot: {
    marginRight: theme.spacing(2),
    height: theme.spacing(5),
    width: theme.spacing(5),
  },
}));


function App() {
  const classes = useRootStyles();
  const snackBarClasses = useSnackBarStyles();

  return (
    <div className={classes.root}>
      <SnackbarProvider
        maxSnack={3}
        anchorOrigin={{ horizontal: 'center', vertical: 'bottom' }}
        classes={snackBarClasses}
        iconVariant={{
          error: <InfoIcon classes={{ root: classes.iconRoot }} fontSize="20px" />,
        }}
      >
        <AppRoute />
      </SnackbarProvider>
    </div>
  );
}

export default withThemeProvider(withAppLayout(App));
