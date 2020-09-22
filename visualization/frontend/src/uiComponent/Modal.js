import React from 'react';
import PropTypes from 'prop-types';

import { withStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import MuiDialogTitle from '@material-ui/core/DialogTitle';
import MuiDialogContent from '@material-ui/core/DialogContent';
import MuiDialogActions from '@material-ui/core/DialogActions';
import IconButton from '@material-ui/core/IconButton';
import CloseIcon from '@material-ui/icons/Close';
import Typography from '@material-ui/core/Typography';
import ButtonGroup from './ButtonGroup';

const styles = (theme) => ({
  root: {
    margin: 0,
    padding: theme.spacing(4),
  },
  closeButton: {
    position: 'absolute',
    right: theme.spacing(2),
    top: theme.spacing(2),
    color: theme.palette.grey[500],
  },
});

const DialogContent = withStyles((theme) => ({
  root: {
    padding: theme.spacing(4),
  },
}))(MuiDialogContent);

const DialogActions = withStyles((theme) => ({
  root: {
    margin: 0,
    padding: theme.spacing(4),
  },
}))(MuiDialogActions);

const DialogTitle = withStyles(styles)((props) => {
  const { children, classes, onClose, ...other } = props;
  return (
    <MuiDialogTitle disableTypography className={classes.root} {...other}>
      <Typography variant="h6">{children}</Typography>
      <IconButton
        aria-label="close"
        data-testid="button-icon-close"
        className={classes.closeButton}
        onClick={onClose}
      >
        <CloseIcon />
      </IconButton>
    </MuiDialogTitle>
  );
});

function Modal({ open, handleClose, title, children, actions }) {
  return (
    <Dialog onClose={handleClose} aria-labelledby="customized-dialog-title" open={open}>
      <DialogTitle id="customized-dialog-title" onClose={handleClose}>
        {title}
      </DialogTitle>
      <DialogContent>{children}</DialogContent>
      {actions.length !== 0 && (
        <DialogActions>
          <ButtonGroup>
            {actions.map(({ name, handleClick, type, isDisable }) => (
              <Button
                key={name}
                data-testid={`button-${name}`}
                autoFocus
                onClick={handleClick}
                color={type}
                variant="contained"
                disabled={isDisable}
              >
                {name}
              </Button>
            ))}
          </ButtonGroup>
        </DialogActions>
      )}
    </Dialog>
  );
}

Modal.defaultProps = {
  actions: [],
};

Modal.propTypes = {
  open: PropTypes.bool.isRequired,
  handleClose: PropTypes.func.isRequired,
  title: PropTypes.string.isRequired,
  children: PropTypes.oneOfType([PropTypes.element, PropTypes.string]).isRequired,
  actions: PropTypes.arrayOf(
    PropTypes.shape({
      name: PropTypes.string.isRequired,
      handleClick: PropTypes.func.isRequired,
      type: PropTypes.oneOf(['primary', 'secondary']).isRequired,
      isDisable: PropTypes.bool,
    }),
  ),
};

export default Modal;
