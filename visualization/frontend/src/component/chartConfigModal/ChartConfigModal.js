import React from 'react';
import PropTypes from 'prop-types';

import { Box } from '@material-ui/core';

import useOldForm from '../../hook/useOldForm';
import chartConfigs from '../../config/chartConfigs';
import Modal from '../../uiComponent/Modal';
import { datasourceValidator } from '../../utils/validators';
import DatasourceSelector from './DatasourceSelector';
import ChartConfigSelector from './ChartConfigSelector';
import useFetch from '../../hook/useFetch';
import { api } from '../../utils/api';
import styles from './chartConfigModalCss';
import { createConfigOptionValidationSchema } from '../../config/chartConfigOptions';

function ChartConfigModal({ open, onCancel, onOk, chartType }) {
  const {
    values,
    validateAndSetValue,
    errors,
    shouldEnableSubmit,
    onSubmit,
    resetFields,
  } = useOldForm({
    ...createConfigOptionValidationSchema(chartConfigs[chartType].configOptions),
    dataSource: datasourceValidator,
  });

  const classes = styles();

  const { data: datasources, loadingState } = useFetch(api.getDatasources);

  async function handleDataSourceChange(dataSourceId) {
    resetFields(chartConfigs[chartType].configOptions);
    validateAndSetValue('dataSource', dataSourceId);
  }

  function handleOk() {
    onSubmit((value) => {
      onOk(value);
    });
  }

  const modalActions = [
    {
      name: 'Ok',
      handleClick: handleOk,
      variant: 'contained',
      color: 'primary',
      isDisable: !shouldEnableSubmit(),
    },
    { name: 'Cancel', handleClick: onCancel, variant: 'outlined' },
  ];

  return (
    <Modal handleClose={onCancel} open={open} title="Configure Chart" actions={modalActions}>
      <Box className={classes.configContent} p={2}>
        <DatasourceSelector
          value={values.dataSource}
          error={errors.dataSource}
          handleDataSourceChange={handleDataSourceChange}
          datasources={datasources && datasources.dataSources}
          loadingState={loadingState}
        />
        {!!values.dataSource && (
          <ChartConfigSelector
            dataSourceId={values.dataSource}
            chartType={chartType}
            updateConfigState={validateAndSetValue}
            errors={errors}
            values={values}
          />
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
};

export default ChartConfigModal;
