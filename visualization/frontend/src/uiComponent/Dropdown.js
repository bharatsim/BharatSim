import React from 'react';
import PropTypes from 'prop-types';

import {
  FormControl,
  FormHelperText,
  InputLabel,
  makeStyles,
  MenuItem,
  Select,
} from '@material-ui/core';

const useStyles = makeStyles((theme) => ({
  formControl: {
    margin: theme.spacing(1),
    minWidth: 200,
  },
  selectEmpty: {
    marginTop: theme.spacing(2),
  },
}));

const renderMenuItems = (id, options) => {
  return (
    options &&
    options.map(({ value, displayName }, index) => {
      const key = `${displayName}-${index}`;
      return (
        <MenuItem
          value={value}
          key={key}
          id={`${id}-${displayName}`}
          data-testid={`${id}-${displayName}`}
        >
          {displayName}
        </MenuItem>
      );
    })
  );
};

export default function Dropdown({ label, options, id, error, value, onChange, ...rest }) {
  const classes = useStyles();

  const handleChange = (event) => {
    onChange(event.target.value);
  };

  return (
    <FormControl variant="outlined" className={classes.formControl} error={!!error}>
      <InputLabel id="dropdown-label">{label}</InputLabel>
      <Select
        labelId="dropdown-label"
        id={id}
        value={value}
        onChange={handleChange}
        label={label}
        data-testid={id}
        MenuProps={{ id: `menu-${id}` }}
        {...rest}
      >
        {renderMenuItems(id, options)}
      </Select>
      {!!error && <FormHelperText error>{error}</FormHelperText>}
    </FormControl>
  );
}

const valuePropType = PropTypes.oneOfType([
  PropTypes.string,
  PropTypes.shape({}),
  PropTypes.number,
]);

Dropdown.propTypes = {
  label: PropTypes.string.isRequired,
  options: PropTypes.arrayOf(
    PropTypes.shape({
      value: valuePropType.isRequired,
      displayName: PropTypes.string.isRequired,
    }),
  ).isRequired,
  id: PropTypes.string.isRequired,
  error: PropTypes.string.isRequired,
  value: valuePropType.isRequired,
  onChange: PropTypes.func.isRequired,
};
