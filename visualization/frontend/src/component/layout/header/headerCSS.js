import { makeStyles } from '@material-ui/core/styles';

export default makeStyles((theme) => ({
  mainContainer: {
    display: 'flex',
    height: theme.spacing(16),
    background: theme.colors.primaryColorScale[700],
  },
  logo: {
    display: 'flex',
    alignItems: 'center',
    padding: theme.spacing(3, 0, 3, 6),
  },
}));
