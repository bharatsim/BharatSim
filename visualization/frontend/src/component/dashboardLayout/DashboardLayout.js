import React, { useState } from 'react';
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

const GridLayout = WidthProvider(ReactGridLayout);
const cols = 12;

function DashboardLayout({ classes }) {
  const [widgets, setWidgets] = useState([]);
  const [count, setCount] = useState(0);
  const [layout, setLayout] = useState();
  const [chartType, setChartType] = useState();

  const { isOpen, closeModal, openModal } = useModal();

  function addItem(config) {
    setWidgets((prevWidgets) => {
      return prevWidgets.concat({
        ...getNewWidgetLayout(prevWidgets.length, cols, count),
        config,
        chartType,
      });
    });
    setCount((prevCount) => prevCount + 1);
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

  return (
    <Box pl={10} pt={10} pr={10}>
      <Box pb={2}>
        <FileUpload />
      </Box>
      <Box pb={2}>
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
