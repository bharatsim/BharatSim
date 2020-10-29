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
      height: theme.spacing(10),
      boxShadow: 'unset',
    },
    sizeSmall: {
      padding: theme.spacing(2, 4),
      height: theme.spacing(8),
    },
    text: {
      boxShadow: 'unset',
      color: theme.colors.primaryColorScale['500'],
      '&:hover': {
        backgroundColor: fade(theme.colors.grayScale['100'], 0.7),
      },
      '&:focus': {
        backgroundColor: fade(theme.colors.grayScale['100'], 0.7),
      },
      '&$disabled': {
        color: theme.palette.text.disabled,
      },
    },
    containedPrimary: {
      boxShadow: 'unset',
      color: theme.colors.button.color,
      backgroundColor: theme.colors.primaryColorScale['500'],
      '&:focus': {
        backgroundColor: fade(theme.colors.primaryColorScale['500'], 0.8),
      },
      '&:hover': {
        backgroundColor: fade(theme.colors.primaryColorScale['500'], 0.8),
      },
      '&$disabled': {
        color: theme.palette.text.disabled,
        backgroundColor: theme.colors.grayScale['100'],
      },
    },
    containedSecondary: {
      boxShadow: 'unset',
      color: theme.colors.primaryColorScale['500'],
      backgroundColor: theme.colors.primaryColorScale['50'],
      '&:focus': {
        backgroundColor: theme.colors.primaryColorScale[50],
      },
      '&:hover': {
        backgroundColor: theme.colors.primaryColorScale[50],
      },
      '&$disabled': {
        color: theme.palette.text.disabled,
        backgroundColor: theme.colors.grayScale['100'],
      },
    },
    outlined: {
      boxShadow: 'unset',
      border: '1px solid',
      color: theme.colors.primaryColorScale['500'],
      borderColor: theme.colors.button.borderColor,
      '&: focus': {
        backgroundColor: fade(theme.colors.primaryColorScale['500'], 0.8),
      },
      '&: hover': {
        backgroundColor: fade(theme.colors.primaryColorScale['500'], 0.8),
      },
    },
  },
  MuiTab: {
    root: {
      padding: theme.spacing(1, 2),
      minHeight: 'unset',
      minWidth: 'unset',
      height: theme.spacing(8),
      boxSizing: 'border-box',
      ...typography.body2,
      fontWeight: 500,
      [theme.breakpoints.up('sm')]: {
        minWidth: 'unset',
      },
    },
    textColorInherit: {
      color: theme.palette.text.secondary,
      borderBottom: '2px solid transparent',
      opacity: 'unset',
      '&$selected': {
        opacity: 'unset',
        color: theme.colors.primaryColorScale['500'],
        borderBottom: '2px solid',
        borderColor: theme.palette.primary.main,
      },
    },
  },
  MuiTabs: {
    root: {
      minHeight: theme.spacing(8),
    },
    indicator: {
      backgroundColor: 'transparent',
    },
  },
  MuiGrid: {
    root: {
      margin: 0,
    },
  },
  MuiStepper: {
    root: {
      backgroundColor: 'transparent',
      padding: 0,
    },
  },
  MuiStepIcon: {
    root: {
      display: 'block',
      color: theme.palette.text.disabled,
      '&$completed': {
        color: theme.colors.primaryColorScale['500'],
      },
      '&$active': {
        color: theme.colors.primaryColorScale['500'],
      },
    },
  },
  MuiLink: {
    root: {
      color: theme.colors.primaryColorScale['500'],
    },
  },
  MuiDialog: {
    paperWidthSm: {
      maxWidth: 'unset',
    },
  },
  MuiDialogContent: {
    dividers: {
      padding: theme.spacing(4),
    },
  },
  MuiToolbar: {
    regular: {
      minHeight: 'unset !important',
    },
  },
};

export default theme;
