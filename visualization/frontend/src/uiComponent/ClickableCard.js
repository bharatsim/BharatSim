import React from 'react';
import Box from '@material-ui/core/Box';
import { makeStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import { ChildrenPropTypes } from '../commanPropTypes';

const useStyles = makeStyles((theme) => {
  return {
    ClickableCardContainer: {
      display: 'flex',
      flexDirection: 'column',
      justifyContent: 'center',
      alignItems: 'center',
      border: '1px dashed',
      borderRadius: theme.spacing(1),
      borderColor: theme.colors.primaryColorScale['200'],
      '&:hover': {
        backgroundColor: theme.colors.primaryColorScale['50'],
        cursor: 'pointer',
      },
    },
  };
});

function ClickableCard({ children, onClick }) {
  const classes = useStyles();
  return (
    <Box onClick={onClick} className={classes.ClickableCardContainer}>
      {children}
    </Box>
  );
}

ClickableCard.propTypes = {
  onClick: PropTypes.func.isRequired,
  children: ChildrenPropTypes.isRequired,
};

export default ClickableCard;
