import React from 'react';
import PropTypes from 'prop-types';
import Table from '../../uiComponent/table/Table';
import fileTypes from '../../constants/fileTypes';
import { formatDate } from '../../utils/dateUtils';

const BYTE_TO_MB_CONVERTOR_UNIT = 1024 * 1024;

function convertFileSizeToMB(fileSize) {
  return `${(fileSize / BYTE_TO_MB_CONVERTOR_UNIT).toFixed(2)}MB`;
}

function DashboardDataSetsTable({ dataSources }) {
  return (
    <div style={{ width: '100%' }}>
      <Table
        data={dataSources}
        columns={[
          { title: 'Name', field: 'name' },
          {
            title: 'Size',
            field: 'fileSize',
            type: 'numeric',
            render: (rowData) => convertFileSizeToMB(rowData.fileSize),
          },
          { title: 'Type', field: 'fileType', render: (rowData) => fileTypes[rowData.fileType] },
          {
            title: 'Date Added',
            field: 'createdAt',
            type: 'datetime',
            render: (rowData) => formatDate(rowData.createdAt),
          },
        ]}
        title="table"
        options={{
          toolbar: false,
          paging: false,
          sorting: false,
        }}
        style={{
          boxShadow: 'none',
          overflow: 'hidden',
        }}
      />
    </div>
  );
}

DashboardDataSetsTable.propTypes = {
  dataSources: PropTypes.arrayOf(
    PropTypes.shape({
      name: PropTypes.string,
      fileSize: PropTypes.number,
      fileTypes: PropTypes.string,
      createdAt: PropTypes.string,
    }),
  ).isRequired,
};

export default DashboardDataSetsTable;
