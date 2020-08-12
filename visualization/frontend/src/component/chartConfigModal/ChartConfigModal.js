import React from 'react';
import PropTypes from 'prop-types';

import {
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Typography,
  withStyles,
} from '@material-ui/core';

import useFetch from '../../hook/useFetch';
import { fetch } from '../../utils/fetch';
import { url } from '../../utils/url';
import Dropdown from '../../uiComponent/Dropdown';
import { convertObjectArrayToOptions, updateState } from '../../utils/helper';
import renderChartConfig from '../chartConfigOptions/renderChartConfig';

import styles from './chartConfigModalCss';
import chartConfig from '../../config/chartConfig';

function ChartConfigModal({ open, onCancel, onOk, chartType, classes }) {
  const [config, setConfig] = React.useState({});
  const [headers, setHeaders] = React.useState([]);
  const dataSources = useFetch({ url: url.DATA_SOURCES });

  if (!dataSources) {
    return null;
  }

  const updateConfigState = (newConfig) => {
    setConfig((prevState) => updateState(prevState, newConfig));
  };

  const handleDataSourceChange = async (value) => {
    const csvHeaders = await fetch({ url: url.getHeaderUrl(value) });
    updateConfigState({ dataSource: value });
    setHeaders(csvHeaders.headers);
  };

  const handleOk = () => {
    onOk(config);
  };

  const chartConfigProps = { headers, updateConfigState };

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
            <Box className={classes.root}>
              <Dropdown
                options={convertObjectArrayToOptions(dataSources.dataSources, '_id', 'name')}
                onChange={handleDataSourceChange}
                id="dropdown-dataSources"
                label="select data source"
              />
              {!!headers.length &&
                renderChartConfig(chartConfig[chartType].configOptions, chartConfigProps)}
            </Box>
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
  chartType: PropTypes.string.isRequired,
  classes: PropTypes.shape({
    root: PropTypes.string,
  }).isRequired,
};

export default withStyles(styles)(ChartConfigModal);
export { ChartConfigModal };
