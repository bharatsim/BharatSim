import { createMuiTheme } from '@material-ui/core/styles';
import { muiColorPalette, colors } from './colorPalette';

const THEME_UNIT_SPACING = 4;

const theme = createMuiTheme({
  colors,
  typography: {
    fontFamily: '"Roboto", sans-serif',
  },
  palette: muiColorPalette,
  spacing: THEME_UNIT_SPACING,
});

export default theme;
