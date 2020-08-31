import React from 'react';
import PropTypes from 'prop-types';

import { Box, Typography, withStyles } from '@material-ui/core';
import Dropdown from '../../uiComponent/Dropdown';
import renderChartConfig from '../chartConfigOptions/renderChartConfig';
import styles from './chartConfigModalCss';

import useFetch from '../../hook/useFetch';
import useForm from '../../hook/useForm';
import chartConfigs from '../../config/chartConfigs';
import Modal from '../../uiComponent/Modal';
import { api } from '../../utils/api';
import { datasourceValidator } from '../../utils/validators';
import { convertObjectArrayToOptionStructure } from '../../utils/helper';

function ChartConfigModal({ open, onCancel, onOk, chartType, classes }) {
  const [headers, setHeaders] = React.useState([]);
  const datasources = useFetch(api.getDatasources);
  const {
    values,
    validateAndSetValue,
    errors,
    shouldEnableSubmit,
    onSubmit,
    resetFields,
  } = useForm({
    ...chartConfigs[chartType].configOptionValidationSchema,
    dataSource: datasourceValidator,
  });

  if (!datasources) {
    return null;
  }

  async function handleDataSourceChange(dataSourceId) {
    resetFields(chartConfigs[chartType].configOptions);
    validateAndSetValue('dataSource', dataSourceId);
    const csvHeaders = await api.getCsvHeaders(dataSourceId);
    setHeaders(csvHeaders.headers);
  }

  function handleOk() {
    onSubmit((value) => {
      onOk(value);
    });
  }

  const chartConfigProps = { headers, updateConfigState: validateAndSetValue, errors, values };

  const isDatasourcePresent = datasources.dataSources.length !== 0;

  const modalActions = [
    { name: 'Ok', handleClick: handleOk, type: 'primary', isDisable: !shouldEnableSubmit() },
    { name: 'Cancel', handleClick: onCancel, type: 'secondary' },
  ];

  return (
    <Modal
      handleClose={onCancel}
      open={open}
      title="Configure Chart"
      actions={isDatasourcePresent ? modalActions : []}
    >
      <Box className={classes.root} p={2}>
        {isDatasourcePresent ? (
          <Box>
            <Dropdown
              options={convertObjectArrayToOptionStructure(datasources.dataSources, 'name', '_id')}
              onChange={handleDataSourceChange}
              id="dropdown-dataSources"
              label="select data source"
              error={errors.dataSource || ''}
              value={values.dataSource || ''}
            />
            {!!headers.length &&
              renderChartConfig(chartConfigs[chartType].configOptions, chartConfigProps)}
          </Box>
        ) : (
          <Box>
            <Typography>No data source present, upload data source</Typography>
          </Box>
        )}
      </Box>
    </Modal>
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
