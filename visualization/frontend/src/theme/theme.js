import { createMuiTheme } from '@material-ui/core/styles';
import colorPalette from './colorPalette';

const THEME_UNIT_SPACING = 4;

const theme = createMuiTheme({
  typography: {
    fontFamily: '"Roboto", sans-serif',
  },
  palette: colorPalette,
  spacing: THEME_UNIT_SPACING,
});

export default theme;
