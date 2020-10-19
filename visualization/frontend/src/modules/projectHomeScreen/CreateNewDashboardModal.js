import React from 'react';
import PropTypes from 'prop-types';
import Box from '@material-ui/core/Box';
import { makeStyles } from '@material-ui/core/styles';
import Modal from '../../uiComponent/Modal';
import useForm from '../../hook/useForm';
import InputTextField from '../../uiComponent/InputTextField';

const useStyles = makeStyles((theme) => {
  return {
    addProjectModal: {
      width: theme.spacing(150),
    },
  };
});

const inputFields = {
  project: {
    id: 'project-title',
    helperText: 'some text',
    defaultValue: 'Untitled Project',
    label: 'Project Title',
  },
  dashboard: {
    id: 'dashboard-title',
    helperText: '',
    defaultValue: 'Untitled Dashboard',
    label: 'Dashboard Title',
  },
};

function CreateNewDashboardModal({ isOpen, closeModal, onCreate, onlyDashboardField }) {
  const classes = useStyles();
  const { values, errors, handleInputChange } = useForm(
    {
      [inputFields.dashboard.id]: inputFields.dashboard.defaultValue,
      [inputFields.project.id]: inputFields.project.defaultValue,
    },
    {},
  );

  function onChangeInput(event) {
    const { id, value } = event.target;
    handleInputChange(id, value);
  }
  function onFormSubmit() {
    onCreate(values);
  }
  return (
    <Modal
      open={isOpen}
      title="New Dashboard"
      handleClose={closeModal}
      actions={[{ name: 'create', handleClick: onFormSubmit, type: 'contained' }]}
    >
      <Box className={classes.addProjectModal}>
        {!onlyDashboardField && (
          <InputTextField
            label={inputFields.project.label}
            id={inputFields.project.id}
            value={values[inputFields.project.id]}
            helperText={errors[inputFields.project.id] || inputFields.project.helperText}
            error={errors[inputFields.project.id]}
            onChange={onChangeInput}
          />
        )}
        <InputTextField
          label={inputFields.dashboard.label}
          id={inputFields.dashboard.id}
          value={values[inputFields.dashboard.id]}
          helperText={errors[inputFields.dashboard.id] || inputFields.dashboard.helperText}
          error={errors[inputFields.dashboard.id]}
          onChange={onChangeInput}
        />
      </Box>
    </Modal>
  );
}

CreateNewDashboardModal.defaultProps = {
  onlyDashboardField: false,
};

CreateNewDashboardModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  closeModal: PropTypes.func.isRequired,
  onCreate: PropTypes.func.isRequired,
  onlyDashboardField: PropTypes.bool,
};

export default CreateNewDashboardModal;
