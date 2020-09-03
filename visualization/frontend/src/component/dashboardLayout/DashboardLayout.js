import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';

import { Box, Button, withStyles } from '@material-ui/core';
import ReactGridLayout, { WidthProvider } from 'react-grid-layout';

import 'react-grid-layout/css/styles.css';
import 'react-resizable/css/styles.css';
import styles from './dashboardLayoutCss';

import { getNewWidgetLayout, renderElement } from '../../utils/dashboardLayoutUtils';
import labels from '../../constants/labels';
import ChartConfigModal from '../chartConfigModal/ChartConfigModal';
import FileUpload from '../fileUpload/FileUpload';

import useFetch from '../../hook/useFetch';

import useModal from '../../hook/useModal';
import { api } from '../../utils/api';
import useInlineLoader from '../../hook/useInlineLoader';
import ChartSelector from '../chartSelector/ChartSelector';
import InlineLoader from '../loader/InlineLoader';

const GridLayout = WidthProvider(ReactGridLayout);

const cols = 12;

function DashboardLayout({ classes }) {
  const [dashboardConfig, setDashboardConfig] = useState({
    name: 'dashboard1',
    id: null,
    count: 0,
  });
  const [widgets, setWidgets] = useState([]);
  const [layout, setLayout] = useState([]);
  const [chartType, setChartType] = useState();

  const { isOpen, closeModal, openModal } = useModal();
  const allDashboards = useFetch(api.getAllDashBoard);
  const {
    loadingState,
    startLoader,
    stopLoaderAfterError,
    stopLoaderAfterSuccess,
  } = useInlineLoader();

  useEffect(() => {
    if (allDashboards && allDashboards.dashboards.length > 0) {
      const firstDashboard = allDashboards.dashboards[0];
      const { name, _id, count } = firstDashboard;
      setWidgets(firstDashboard.widgets);
      setLayout(firstDashboard.layout);
      setDashboardConfig({ name, id: _id, count });
    }
  }, [allDashboards]);

  function addItem(config) {
    setWidgets((prevWidgets) => {
      return prevWidgets.concat({
        config,
        chartType,
        layout: getNewWidgetLayout(prevWidgets.length, cols, dashboardConfig.count),
      });
    });
    setDashboardConfig((prevState) => ({ ...prevState, count: prevState.count + 1 }));
  }

  function handleModalOk(config) {
    addItem(config);
    closeModal();
  }

  function onLayoutChange(changedLayout) {
    setLayout(changedLayout);
  }

  function onChartClick(selectedChartType) {
    openModal();
    setChartType(selectedChartType);
  }
  function onClickOfSaveDashboard() {
    startLoader(`Saving ${dashboardConfig.name}...`);
    api
      .saveDashboard({
        widgets,
        layout,
        dashboardId: dashboardConfig.id,
        name: dashboardConfig.name,
        count: dashboardConfig.count,
      })
      .then((data) => {
        setDashboardConfig((prevState) => ({
          ...prevState,
          id: data.dashboardId,
        }));
        stopLoaderAfterSuccess(`Dashboard ${dashboardConfig.name} Saved Successfully`);
      })
      .catch(() => {
        stopLoaderAfterError(`Failed to save dashboard ${dashboardConfig.name}`);
      });
  }

  return (
    <Box pl={10} pt={10} pr={10}>
      <Box pb={2}>
        <FileUpload />
      </Box>
      <Box pb={2} display="flex" justifyContent="space-between">
        <ChartSelector onClick={onChartClick} />
        <Box>
          <Button onClick={onClickOfSaveDashboard} variant="contained" color="primary">
            {labels.dashboardLayout.SAVE_DASHBOARD_BUTTON}
          </Button>
          <InlineLoader status={loadingState.state} message={loadingState.message} />
        </Box>
      </Box>
      <GridLayout
        layout={layout}
        onLayoutChange={onLayoutChange}
        className={classes.reactGridLayout}
      >
        {widgets.map((item) => {
          return renderElement(item);
        })}
      </GridLayout>
      {isOpen && (
        <ChartConfigModal
          onCancel={closeModal}
          onOk={handleModalOk}
          open={isOpen}
          chartType={chartType}
        />
      )}
    </Box>
  );
}

DashboardLayout.propTypes = {
  classes: PropTypes.shape({
    reactGridLayout: PropTypes.string.isRequired,
  }).isRequired,
};

export default withStyles(styles)(DashboardLayout);
