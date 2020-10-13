import TextField from '@material-ui/core/TextField';
import React from 'react';
import { Box, Grid } from '@material-ui/core';
import PropTypes from 'prop-types';
import { makeStyles } from '@material-ui/core/styles';

const useStyles = makeStyles((theme) => {
  return {
    helperText: {
      margin: theme.spacing(1, 0, 0, 0),
    },
    inputField: {
      padding: theme.spacing(4),
      backgroundColor: theme.colors.grayScale['100'],
      borderRadius: theme.spacing(1),
    },
    textFieldContainer: {
      width: '100%',
      display: 'flex',
      justifyContent: 'space-between',
    },
    labelContainer: {
      display: 'flex',
      paddingTop: theme.spacing(4),
      justifyContent: 'flex-end',
      height: '100%',
    },
    label: {
      wordWrap: 'break-word',
    },
  };
});
function InputTextField({ label, id, value, helperText, error, onChange }) {
  const classes = useStyles();
  return (
    <Grid container xl={12}>
      <Grid item xs={3}>
        <Box className={classes.labelContainer}>
          <label htmlFor={id} className={classes.label}>
            {label}
          </label>
        </Box>
      </Grid>
      <Grid item xs={1} />
      <Grid item xs={7}>
        <TextField
          id={id}
          value={value}
          helperText={helperText || error}
          error={!!error}
          variant="filled"
          fullWidth
          InputProps={{ classes: { input: classes.inputField } }}
          FormHelperTextProps={{ classes: { root: classes.helperText } }}
          onChange={onChange}
        />
      </Grid>
    </Grid>
  );
}

InputTextField.propTypes = {
  label: PropTypes.string.isRequired,
  id: PropTypes.string.isRequired,
  value: PropTypes.string.isRequired,
  helperText: PropTypes.string,
  error: PropTypes.string,
  onChange: PropTypes.func.isRequired,
};

InputTextField.defaultProps = {
  helperText: '',
  error: '',
};

export default InputTextField;
