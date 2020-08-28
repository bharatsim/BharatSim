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
import useModal from '../../hook/useModal';
import { chartTypes } from '../../constants/charts';
import FileUpload from '../fileUpload/FileUpload';
import ButtonGroup from '../../uiComponent/ButtonGroup';
import { fetchData, headerBuilder, uploadData } from '../../utils/fetch';
import { url } from '../../utils/url';
import { contentTypes } from '../../constants/fetch';

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

  useEffect(() => {
    async function fetchApiData() {
      const resData = await fetchData({ url: url.DASHBOARD_URL });
      if (resData.length > 0) {
        const { widgets: savedWidgets, name, _id, layout: savedLayout, count } = resData[0];
        setWidgets(savedWidgets);
        setLayout(savedLayout);
        setDashboardConfig({ name, id: _id, count });
      }
    }

    fetchApiData();
  }, []);

  function addItem(config) {
    setWidgets((prevWidgets) => {
      return prevWidgets.concat({
        layout: getNewWidgetLayout(prevWidgets.length, cols, dashboardConfig.count),
        config,
        chartType,
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
  function saveDashboard() {
    uploadData({
      url: url.DASHBOARD_URL,
      headers: headerBuilder({ contentType: contentTypes.JSON }),
      data: JSON.stringify({
        dashboardData: {
          name: dashboardConfig.name,
          widgets,
          dashboardId: dashboardConfig.id,
          layout,
          count: dashboardConfig.count,
        },
      }),
    }).then((data) => setDashboardConfig((prevState) => ({ ...prevState, id: data.dashboardId })));
  }

  return (
    <Box pl={10} pt={10} pr={10}>
      <Box pb={2}>
        <FileUpload />
      </Box>
      <Box pb={2} display="flex" justifyContent="space-between">
        <ButtonGroup>
          <Button
            onClick={() => oneChartClick(chartTypes.BAR_CHART)}
            variant="contained"
            color="primary"
          >
            {labels.dashboardLayout.Bar_CHART}
          </Button>
          <Button
            onClick={() => oneChartClick(chartTypes.LINE_CHART)}
            variant="contained"
            color="primary"
          >
            {labels.dashboardLayout.LINE_CHART}
          </Button>
        </ButtonGroup>
        <Button onClick={saveDashboard} variant="contained" color="primary">
          {labels.dashboardLayout.SAVE_DASHBOARD_BUTTON}
        </Button>
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
