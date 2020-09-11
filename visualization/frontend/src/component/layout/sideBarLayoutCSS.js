import { makeStyles } from '@material-ui/core/styles';

export default makeStyles((theme) => ({
  main: {
    display: 'flex',
    height: '100%',
  },
  sideBar: {
    display: 'flex',
    flexDirection: 'column',
    background: theme.palette.primary.main,
    width: theme.spacing(64),
    color: theme.palette.common.white,
  },
  view: {
    display: 'flex',
    flex: '1',
    flexDirection: 'column',
  },
  logo: {
    display: 'flex',
    alignItems: 'center',
    padding: theme.spacing(4),
    height: theme.spacing(15),
  },
  logoText: {
    fontWeight: 900,
    fontSize: '18px',
    fontStyle: 'normal',
  },
}));
