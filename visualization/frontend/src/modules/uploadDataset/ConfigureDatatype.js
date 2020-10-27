import React from 'react';
import PropTypes from 'prop-types';
import { Box, useTheme } from '@material-ui/core';

import Table from '../../uiComponent/table/Table';
import DataPreviewTableToolBar from './DataPreviewTableToolBar';
import DataPreviewTableHeader from './DataPreviewTableHeader';
import { createColumnForMTable } from '../../utils/fileUploadUtils';

function ConfigureDatatype({ selectedFile, previewData, schema }) {
  const theme = useTheme();
  return (
    <Box>
      <Table
        title="DataFile"
        columns={createColumnForMTable(schema)}
        data={previewData}
        options={{
          paging: true,
          showTitle: false,
          headerStyle: {
            padding: theme.spacing(1, 2, 1, 2),
            ...theme.typography.subtitle1,
            textAlign: 'left',
            flexDirection: 'row',
            borderBottom: 'unset',
          },
        }}
        components={{
          Toolbar: (props) => <DataPreviewTableToolBar {...props} file={selectedFile} />,
          Header: (props) => <DataPreviewTableHeader {...props} />,
        }}
      />
    </Box>
  );
}

ConfigureDatatype.propTypes = {
  selectedFile: PropTypes.objectOf(File).isRequired,
  previewData: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  schema: PropTypes.shape({}).isRequired,
};

export default ConfigureDatatype;
