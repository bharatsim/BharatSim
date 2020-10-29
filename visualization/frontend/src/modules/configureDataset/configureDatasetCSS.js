import { makeStyles } from '@material-ui/core/styles';

const useConfigureDatasetStyles = makeStyles((theme) => {
  return {
    configureProjectDataBar: {
      height: theme.spacing(12),
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
      margin: theme.spacing(8, 8, 16, 8),
    },
    dashboardDataContainerTitle: {
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'flex-end',
      paddingBottom: theme.spacing(2),
    },
    dashboardDataBody: {
      display: 'flex',
      flexDirection: 'column',
      justifyContent: 'center',
      alignItems: 'center',
      boxSizing: 'border-box',
      boxShadow: 'inset 0px -1px 0px rgba(0, 0, 0, 0.12)',
      border: '1px solid rgba(0, 0, 0, 0.2)',
      borderBottomRightRadius: theme.spacing(1),
      borderBottomLeftRadius: theme.spacing(1),
    },
    noDataSourcesMessage: {
      padding: theme.spacing(6),
      textAlign: 'center',
    },
  };
});

export default useConfigureDatasetStyles;
