import React from 'react';
import PropTypes from 'prop-types';
import MaterialTable from 'material-table';
import { useTheme } from '@material-ui/core';
import tableIcons from './tableIcon';

function Table({ columns, data, title, options, ...rest }) {
  const theme = useTheme();
  return (
    <MaterialTable
      icons={tableIcons}
      title={title}
      columns={columns}
      data={data}
      options={{
        rowStyle: (_, index) => ({
          height: theme.spacing(8),
          backgroundColor: index % 2 ? theme.colors.primaryColorScale[50] : 'transparent',
        }),
        cellStyle: () => ({
          height: theme.spacing(8),
          padding: theme.spacing(2, 3),
          textAlign: 'left',
          ...theme.typography.body2,
          lineHeight: 1,
        }),
        headerStyle: {
          padding: theme.spacing(2, 3),
          textAlign: 'left',
          flexDirection: 'row',
          ...theme.typography.subtitle1,
          lineHeight: 1.5,
        },
        emptyRowsWhenPaging: false,
        disableGutters: true,
        pageSize: 10,
        draggable: false,
        ...options,
      }}
      style={{
        boxShadow: 'none',
        border: '1px solid',
        borderColor: `${theme.colors.primaryColorScale['500']}3D`,
        borderRadius: theme.spacing(1),
        overflow: 'hidden',
      }}
      {...rest}
    />
  );
}

Table.defaultProps = {
  options: {},
};

Table.propTypes = {
  columns: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  data: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  title: PropTypes.string.isRequired,
  options: PropTypes.shape({}),
};

export default Table;
