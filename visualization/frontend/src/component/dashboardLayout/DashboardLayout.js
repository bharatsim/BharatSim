import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';

import { Box, Button, FormHelperText, withStyles } from '@material-ui/core';
import ReactGridLayout, { WidthProvider } from 'react-grid-layout';

import 'react-grid-layout/css/styles.css';
import 'react-resizable/css/styles.css';
import styles from './dashboardLayoutCss';

import { getNewWidgetLayout, renderElement } from '../../utils/dashboardLayoutUtils';
import labels from '../../constants/labels';
import ChartConfigModal from '../chartConfigModal/ChartConfigModal';
import ButtonGroup from '../../uiComponent/ButtonGroup';
import FileUpload from '../fileUpload/FileUpload';

import useFetch from '../../hook/useFetch';

import useModal from '../../hook/useModal';
import chartConfigs from '../../config/chartConfigs';
import { api } from '../../utils/api';

const GridLayout = WidthProvider(ReactGridLayout);

const cols = 12;

function DashboardLayout({ classes }) {
  const [dashboardConfig, setDashboardConfig] = useState({
    name: 'dashboard1',
    id: null,
    count: 0,
    message: null,
    messageType: null,
  });
  const [widgets, setWidgets] = useState([]);
  const [layout, setLayout] = useState([]);
  const [chartType, setChartType] = useState();
  const { isOpen, closeModal, openModal } = useModal();

  const allDashboards = useFetch(api.getAllDashBoard);

  useEffect(() => {
    if (allDashboards && allDashboards.dashboards.length > 0) {
      const firstDashboard = allDashboards.dashboards[0];
      const { name, _id, count } = firstDashboard;
      setWidgets(firstDashboard.widgets);
      setLayout(firstDashboard.layout);
      setDashboardConfig((prevState) => ({ ...prevState, name, id: _id, count }));
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

  function oneChartClick(selectedChartType) {
    openModal();
    setChartType(selectedChartType);
  }
  function onClickOfSaveDashboard() {
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
          message: 'Dashboard Saved Successfully',
          messageType: 'success',
        }));
      })
      .catch(() => {
        setDashboardConfig((prevState) => ({
          ...prevState,
          message: 'Failed to save dashboard',
          messageType: 'error',
        }));
      });
  }

  return (
    <Box pl={10} pt={10} pr={10}>
      <Box pb={2}>
        <FileUpload />
      </Box>
      <Box pb={2} display="flex" justifyContent="space-between">
        <ButtonGroup>
          {Object.values(chartConfigs).map((chart) => {
            return (
              <Button
                key={chart.key}
                onClick={() => oneChartClick(chart.key)}
                variant="contained"
                color="primary"
              >
                {chart.label}
              </Button>
            );
          })}
        </ButtonGroup>
        <Box>
          <Button onClick={onClickOfSaveDashboard} variant="contained" color="primary">
            {labels.dashboardLayout.SAVE_DASHBOARD_BUTTON}
          </Button>
          {!!dashboardConfig.message && (
            <FormHelperText error={dashboardConfig.messageType === 'error'}>
              {dashboardConfig.message}
            </FormHelperText>
          )}
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
