import React from 'react';
import PropTypes from 'prop-types';

import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Typography, Box } from '@material-ui/core';

import useFetch from '../../hook/useFetch';
import { fetch } from '../../utils/fetch';
import { url } from '../../utils/url';
import Dropdown from '../../uiComponent/Dropdown';
import { convertObjectArrayToOptions, convertStringArrayToOptions, updateState } from '../../utils/helper';

function ChartConfigModal({ open, onCancel, onOk }) {
  const [config, setConfig] = React.useState({});

  const [headers, setHeaders] = React.useState([]);

  const dataSources = useFetch({ url: url.DATA_SOURCES });

  if (!dataSources) {
    return null;
  }
  const handleDataSourceChange = async (value) => {
    const csvHeaders = await fetch({ url: url.getHeaderUrl(value) });
    setConfig((prevState) => updateState(prevState, { dataSource: value }));
    setHeaders(csvHeaders.headers);
  };

  const handleXChange = (value) => {
    setConfig((prevState) => updateState(prevState, { xColumn: value }));
  };
  const handleYChange = (value) => {
    setConfig((prevState) => updateState(prevState, { yColumn: value }));
  };

  const handleOk = () => {
    onOk(config);
  };

  return (
    <Dialog open={open} onClose={onCancel} aria-labelledby="form-dialog-title">
      <DialogTitle id="form-dialog-title">Chart Config</DialogTitle>

      {dataSources.dataSources.length === 0 ? (
        <Box p={10}>
          <Typography>No data source present, upload data source</Typography>
        </Box>
      ) : (
        <>
          <DialogContent>
            <Dropdown
              options={convertObjectArrayToOptions(dataSources.dataSources, '_id', 'name')}
              onChange={handleDataSourceChange}
              id="dropdown-dataSources"
              label="select data source"
            />
            <Dropdown
              options={convertStringArrayToOptions(headers)}
              onChange={handleXChange}
              id="dropdown-x"
              label="select x axis"
              disabled={headers.length === 0}
            />
            <Dropdown
              options={convertStringArrayToOptions(headers)}
              onChange={handleYChange}
              id="dropdown-y"
              label="select y axis"
              disabled={headers.length === 0}
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={onCancel} variant="contained" color="secondary">
              Cancel
            </Button>
            <Button onClick={handleOk} variant="contained" color="primary">
              Ok
            </Button>
          </DialogActions>
        </>
      )}
    </Dialog>
  );
}

ChartConfigModal.propTypes = {
  open: PropTypes.bool.isRequired,
  onCancel: PropTypes.func.isRequired,
  onOk: PropTypes.func.isRequired,
};

export default ChartConfigModal;
