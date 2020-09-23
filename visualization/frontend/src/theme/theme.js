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
    sizeLarge: {
      height: theme.spacing(9),
    },
    sizeSmall: {
      height: theme.spacing(7),
    },
    contained: {
      color: theme.colors.button.color,
      backgroundColor: theme.colors.primaryColorScale['500'],
      '&:hover': {
        backgroundColor: fade(theme.colors.primaryColorScale['500'], 0.08),
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
        backgroundColor: fade(theme.colors.primaryColorScale['500'], 0.08),
      },
    },
  },
};
export default theme;
