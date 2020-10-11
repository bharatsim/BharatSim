import React from 'react';
import PropTypes from 'prop-types';
import { Box, Typography } from '@material-ui/core';
import { makeStyles } from '@material-ui/core/styles';

const styles = makeStyles((theme) => ({
  cardContainer: {
    padding: theme.spacing(4, 6),
    height: theme.spacing(32),
    textTransform: 'capitalize',
    boxShadow:
      '0px 1px 5px rgba(0, 0, 0, 0.2), 0px 3px 4px rgba(0, 0, 0, 0.12), 0px 2px 4px rgba(0, 0, 0, 0.14)',
    borderRadius: theme.spacing(1),
    '&:hover': {
      boxShadow:
        '0px 3px 5px rgba(0, 0, 0, 0.2), 0px 1px 18px rgba(0, 0, 0, 0.12), 0px 6px 10px rgba(0, 0, 0, 0.14)',
      cursor: 'pointer',
    },
  },
}));

function ProjectMetadataCard({ project, onProjectClick }) {
  const { name, _id } = project;
  const classes = styles();
  return (
    <Box className={classes.cardContainer} onClick={() => onProjectClick(_id)}>
      <Typography variant="subtitle2">{name}</Typography>
    </Box>
  );
}

ProjectMetadataCard.propTypes = {
  project: PropTypes.shape({
    name: PropTypes.string,
    _id: PropTypes.string,
  }).isRequired,
  onProjectClick: PropTypes.func.isRequired,
};

export default ProjectMetadataCard;
