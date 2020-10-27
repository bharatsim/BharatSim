import React from 'react';
import PropTypes from 'prop-types';
import { MTableHeader } from 'material-table';
import { TableRow, Typography } from '@material-ui/core';
import TableCell from '@material-ui/core/TableCell';
import { makeStyles } from '@material-ui/core/styles';

const useTableCellStyles = makeStyles((theme) => ({
  root: {
    padding: theme.spacing(2, 3),
    backgroundColor: theme.colors.primaryColorScale['50'],
  },
}));

function DataPreviewTableHeader(props) {
  const tableCellClasses = useTableCellStyles();
  const { columns } = props;
  return (
    <>
      <MTableHeader {...props} />
      <thead>
        <TableRow>
          {columns.map((column) => (
            <TableCell classes={tableCellClasses} key={column.title}>
              <Typography variant="caption">Datatype</Typography>
              <Typography variant="subtitle1">{column.dataType}</Typography>
            </TableCell>
          ))}
        </TableRow>
      </thead>
    </>
  );
}

DataPreviewTableHeader.propTypes = {
  columns: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
};

export default DataPreviewTableHeader;
