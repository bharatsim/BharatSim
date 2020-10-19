import { makeStyles } from "@material-ui/core/styles";

const useSnackBarStyles = makeStyles((theme) => ({
  variantSuccess: {
    backgroundColor: `${theme.palette.success.main}1A !important`,
    borderColor: `${theme.palette.success.main}33`,
    border: '1px solid',
    color: `${theme.palette.success.dark} !important`,
    ...theme.typography.body2,
    borderRadius: theme.spacing(1),
    boxShadow: 'unset',
    opacity: 1,
    minWidth: theme.spacing(120),
  },
  variantError: {
    backgroundColor: `${theme.palette.error.light}1A !important`,
    borderColor: `${theme.palette.error.light}33`,
    border: '1px solid',
    color: `${theme.palette.error.dark} !important`,
    ...theme.typography.body2,
    borderRadius: theme.spacing(1),
    boxShadow: 'unset',
    opacity: 1,
    minWidth: theme.spacing(120),
  },
}));

export default useSnackBarStyles;