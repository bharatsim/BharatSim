import React, { useState } from 'react';
import PropTypes from 'prop-types';

import { makeStyles, InputLabel, MenuItem, FormControl, Select } from '@material-ui/core';

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
    options.map(({ value, displayName }) => {
      return (
        <MenuItem value={value} key={value} id={`${id}-${value}`} data-testid={`${id}-${value}`}>
          {displayName}
        </MenuItem>
      );
    })
  );
};

export default function Dropdown({ label, options, id, onChange, ...rest }) {
  const classes = useStyles();
  const [value, setValue] = useState('');

  const handleChange = (event) => {
    setValue(event.target.value);
    onChange(event.target.value);
  };

  return (
    <FormControl variant="outlined" className={classes.formControl}>
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
    </FormControl>
  );
}

Dropdown.propTypes = {
  label: PropTypes.string.isRequired,
  options: PropTypes.arrayOf(
    PropTypes.shape({
      value: PropTypes.string.isRequired,
      displayName: PropTypes.string.isRequired,
    }),
  ).isRequired,
  id: PropTypes.string.isRequired,
  onChange: PropTypes.func.isRequired,
};
