import { makeStyles } from '@material-ui/core/styles';

const useTabStyles = makeStyles((theme) => ({
  root: {
    padding: theme.spacing(3, 7),
    height: theme.spacing(10),
    borderRadius: theme.spacing(1),
    minHeight: 'unset',
    minWidth: 'unset',
    boxSizing: 'border-box',
    ...theme.typography.subtitle2,
    [theme.breakpoints.up('sm')]: {
      minWidth: 'unset',
    },
  },
  wrapper: {
    display: 'flex',
    flexDirection: 'row',
    '& > *:first-child': {
      marginBottom: '0 !important',
    },
    justifyContent: 'left',
  },
  labelIcon: {
    minHeight: '40px',
    height: '40px',
  },
  selected: {
    background: theme.colors.primaryColorScale['500'],
  },
  textColorInherit: {
    border: 'none',
    color: theme.colors.textLight.primary,
    textTransform: 'capitalize',
    opacity: 'unset',
    '&$selected': {
      border: 'none',
      opacity: 'unset',
      color: theme.colors.textLight.primary,
    },
  },
}));

const useTabsStyles = makeStyles(() => ({
  indicator: {
    background: 'transparent',
  },
}));

export { useTabStyles, useTabsStyles };
