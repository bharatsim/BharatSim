import React, { useState } from 'react';
import { Box, Typography } from '@material-ui/core';
import { makeStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import Modal from '../../uiComponent/Modal';
import { createSchema } from '../../utils/fileUploadUtils';
import Dropdown from '../../uiComponent/Dropdown';
import { convertStringArrayToOptions } from '../../utils/helper';
import dataTypesMapping from '../../constants/dataTypesMapping';

const dropdownStyles = makeStyles(() => ({
  configDropdown: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    width: '100%',
  },
}));

const styles = makeStyles((theme) => ({
  configContainer: {
    width: theme.spacing(140),
    height: theme.spacing(100),
    padding: theme.spacing(4),
  },
}));

function renderDropdown(columnName, value, onChange) {
  const classes = dropdownStyles();
  return (
    <Box className={classes.configDropdown} key={columnName}>
      <Typography>{columnName}</Typography>
      <Dropdown
        label="Select datatype"
        value={value}
        options={convertStringArrayToOptions(Object.values(dataTypesMapping))}
        id={columnName}
        onChange={(selectedValue) => onChange(columnName, selectedValue)}
      />
    </Box>
  );
}

function DataTypeConfigModal({ dataRow, isOpen, closeModal, onApply, onCancel }) {
  const schema = createSchema(dataRow);
  const [values, setValues] = useState(schema);
  const classes = styles();

  function onChange(key, value) {
    setValues((prevState) => ({ ...prevState, [key]: value }));
  }

  return (
    <Modal
      handleClose={closeModal}
      open={isOpen}
      title="Configure Datatype"
      actions={[
        { name: 'cancel', handleClick: onCancel, type: 'outlined' },
        { name: 'apply and upload', handleClick: () => onApply(values), type: 'contained' },
      ]}
    >
      <Box className={classes.configContainer}>
        {Object.keys(schema).map((columnName) =>
          renderDropdown(columnName, values[columnName], onChange),
        )}
      </Box>
    </Modal>
  );
}

DataTypeConfigModal.propTypes = {
  dataRow: PropTypes.shape({}).isRequired,
  isOpen: PropTypes.bool.isRequired,
  closeModal: PropTypes.func.isRequired,
  onApply: PropTypes.func.isRequired,
  onCancel: PropTypes.func.isRequired,
};

export default DataTypeConfigModal;
