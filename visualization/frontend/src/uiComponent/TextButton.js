import Button from '@material-ui/core/Button';
import styled from '@material-ui/core/styles/styled';

const TextButton = styled(Button)(({ theme }) => ({
  color: theme.colors.primaryColorScale['500'],
}));

export default TextButton;
