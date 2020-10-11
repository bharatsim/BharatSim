import { makeStyles } from '@material-ui/core/styles';

const useProjectLayoutStyle = makeStyles((theme) => {
  return {
    projectNameBar: {
      textTransform: 'capitalize',
      boxShadow: '0px 1px 1px rgba(78, 96, 176, 0.3)',
      height: theme.spacing(12),
      padding: theme.spacing(0, 8),
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
    },
    layoutContainer: {
      display: 'flex',
      width: '100%',
      height: '100%',
      minHeight: 'calc(100vh - 64px)',
    },
    sideBarLayout: {
      display: 'flex',
      flexDirection: 'column',
      background: theme.colors.primaryColorScale['600'],
      width: theme.spacing(64),
      color: theme.colors.textLight.primary,
      paddingTop: theme.spacing(18),
    },
  };
});

export default useProjectLayoutStyle;
