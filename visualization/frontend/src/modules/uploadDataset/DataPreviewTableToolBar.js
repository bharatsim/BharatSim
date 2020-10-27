import { Box, Chip, Typography } from '@material-ui/core';
import PropTypes from 'prop-types';
import { MTableToolbar } from 'material-table';
import React from 'react';

function DataPreviewTableToolBar({ file, ...mTableProps }) {
  return (
    <Box display="flex" alignItems="center" justifyContent="space-between" p={2}>
      <Box display="flex" alignItems="center">
        <Typography variant="body2">DataFile:</Typography>
        <Box pl={2}>
          <Chip variant="outlined" size="small" label={file.name} color="secondary" />
        </Box>
      </Box>
      <MTableToolbar {...mTableProps} />
    </Box>
  );
}

DataPreviewTableToolBar.propTypes = {
  file: PropTypes.objectOf(File).isRequired,
};

export default DataPreviewTableToolBar;
