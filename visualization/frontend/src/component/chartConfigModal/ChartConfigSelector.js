import React from 'react';
import PropTypes from 'prop-types';

import chartConfigs from '../../config/chartConfigs';
import useFetch from '../../hook/useFetch';
import { api } from '../../utils/api';
import chartConfigOptions from '../../config/chartConfigOptions';
import LoaderOrError from '../loaderOrError/LoaderOrError';

function ChartConfigSelector({ dataSourceId, chartType, updateConfigState, errors, values }) {
  const { data: csvHeaders, loadingState } = useFetch(api.getCsvHeaders, {
    data: [dataSourceId],
  });

  const { headers } = csvHeaders || {};
  const chartConfigProps = { headers, updateConfigState, errors, values };
  const configOptionsKeysForSelectedChart = chartConfigs[chartType].configOptions;

  return (
    <LoaderOrError loadingState={loadingState}>
      <div>
        {headers && (
          <div>
            {configOptionsKeysForSelectedChart.map((chartConfigKey) => (
              <div key={chartConfigKey}>
                {chartConfigOptions[chartConfigKey].component(chartConfigProps)}
              </div>
            ))}
          </div>
        )}
      </div>
    </LoaderOrError>
  );
}

ChartConfigSelector.propTypes = {
  dataSourceId: PropTypes.string.isRequired,
  chartType: PropTypes.string.isRequired,
  updateConfigState: PropTypes.func.isRequired,
  errors: PropTypes.shape({}).isRequired,
  values: PropTypes.shape({}).isRequired,
};

export default ChartConfigSelector;
