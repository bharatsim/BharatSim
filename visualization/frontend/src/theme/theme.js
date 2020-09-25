import { createMuiTheme } from '@material-ui/core/styles';
import { fade } from '@material-ui/core';
import { muiColorPalette, colors } from './colorPalette';
import { typography } from './typography';

const THEME_UNIT_SPACING = 4;

const theme = createMuiTheme({
  colors,
  typography,
  palette: muiColorPalette,
  spacing: THEME_UNIT_SPACING,
});

theme.overrides = {
  MuiButton: {
    root: {
      padding: theme.spacing(2, 4),
      height: theme.spacing(8),
    },
    sizeLarge: {
      padding: theme.spacing(2, 4),
      height: theme.spacing(9),
    },
    sizeSmall: {
      padding: theme.spacing(2, 4),
      height: theme.spacing(8),
    },
    contained: {
      color: theme.colors.button.color,
      backgroundColor: theme.colors.primaryColorScale['500'],
      '&:hover': {
        backgroundColor: fade(theme.colors.primaryColorScale['500'], 0.8),
      },
      '&$disabled': {
        color: theme.palette.text.disabled,
        backgroundColor: theme.colors.grayScale['100'],
      },
    },
    outlined: {
      border: '1px solid',
      color: theme.colors.primaryColorScale['500'],
      borderColor: theme.colors.button.borderColor,
      '&: focus': {
        backgroundColor: fade(theme.colors.primaryColorScale['500'], 0.2),
      },
      '&: hover': {
        backgroundColor: fade(theme.colors.primaryColorScale['500'], 0.8),
      },
    },
  },
  MuiTab: {
    root: {
      padding: theme.spacing(2),
      minHeight: 'unset',
      minWidth: 'unset',
      height: theme.spacing(9),
      boxSizing: 'border-box',
      ...typography.subtitle2,
      [theme.breakpoints.up('sm')]: {
        minWidth: 'unset',
      },
    },
    textColorInherit: {
      color: theme.palette.text.secondary,
      '&$selected': {
        color: theme.palette.primary.main,
        borderBottom: '2px solid',
        borderColor: theme.palette.primary.main,
      },
    },
    borderColor: 'red',
  },
  MuiTabs: {
    root: {
      minHeight: theme.spacing(9),
    },
    indicator: {
      backgroundColor: 'transparent',
    },
  },
};

export default theme;
