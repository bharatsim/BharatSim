import { makeStyles } from '@material-ui/core/styles';

const useConfigureDatasetStyles = makeStyles((theme) => {
  return {
    configureProjectDataBar: {
      height: theme.spacing(16),
      width: '100%',
      display: 'flex',
      justifyContent: 'space-between',
      backgroundColor: theme.colors.grayScale['100'],
      alignItems: 'center',
      padding: theme.spacing(0, 8),
    },
    dashboardDataHeader: {
      display: 'flex',
      alignItems: 'center',
      height: theme.spacing(8),
      paddingLeft: theme.spacing(4),
      paddingRight: theme.spacing(4),
      borderTopRightRadius: theme.spacing(1),
      borderTopLeftRadius: theme.spacing(1),
      backgroundColor: theme.colors.primaryColorScale['500'],
      boxShadow: 'inset 0px -1px 0px rgba(0, 0, 0, 0.12)',
      color: theme.colors.textLight.primary,
      textTransform: 'capitalize',
    },
    dashboardDataContainer: {
      width: 'calc(100% - 64px)',
      maxWidth: theme.spacing(256),
      margin: theme.spacing(8, 8, 16, 8),
    },
    dashboardDataBody: {
      display: 'flex',
      flexDirection: 'column',
      justifyContent: 'center',
      alignItems: 'center',
      boxSizing: 'border-box',
      boxShadow: 'inset 0px -1px 0px rgba(0, 0, 0, 0.12)',
      height: theme.spacing(22),
      border: '1px solid rgba(0, 0, 0, 0.2)',
    },
  };
});

export default useConfigureDatasetStyles;
