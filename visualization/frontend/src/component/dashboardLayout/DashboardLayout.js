import React, { useState } from 'react';
import PropTypes from 'prop-types';

import { Box, Button, withStyles } from '@material-ui/core';
import ReactGridLayout, { WidthProvider } from 'react-grid-layout';

import 'react-grid-layout/css/styles.css';
import 'react-resizable/css/styles.css';
import styles from './dashboardLayoutCss';

import { renderElement, getNewWidgetLayout } from './utils';
import labels from '../../constants/labels';
import ChartConfigModal from '../chartConfigModal/ChartConfigModal';
import useModal from '../../hook/useModal';
import FileUpload from '../../uiComponent/FileUpload';
import { chartTypes } from '../../constants/charts';

const GridLayout = WidthProvider(ReactGridLayout);
const cols = 12;

const DashboardLayout = ({ classes }) => {
  const [widgets, setWidgets] = useState([]);
  const [count, setCount] = useState(0);
  const [layout, setLayout] = useState();
  const [chartType, setChartType] = useState();

  const { isOpen, closeModal, openModal } = useModal();

  const addItem = (config) => {
    setWidgets((prevWidgets) => {
      return prevWidgets.concat({
        ...getNewWidgetLayout(prevWidgets.length, cols, count),
        config,
        chartType,
      });
    });
    setCount((prevCount) => prevCount + 1);
  };

  const handleModalOk = (config) => {
    addItem(config);
    closeModal();
  };

  const onLayoutChange = (changedLayout) => {
    setLayout(changedLayout);
  };
  const oneChartClick = (selectedChartType) => {
    openModal();
    setChartType(selectedChartType);
  };

  return (
    <Box pl={10} pt={10} pr={10}>
      <Box pb={2}>
        <FileUpload />
      </Box>
      <Box pb={2}>
        <Button
          onClick={() => oneChartClick(chartTypes.BAR_CHART)}
          variant="contained"
          color="primary"
          className={classes.buttonRoot}
        >
          {labels.dashboardLayout.Bar_CHART}
        </Button>
        <Button
          onClick={() => oneChartClick(chartTypes.LINE_CHART)}
          variant="contained"
          color="primary"
          className={classes.buttonRoot}
        >
          {labels.dashboardLayout.LINE_CHART}
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
};

DashboardLayout.propTypes = {
  classes: PropTypes.shape({
    buttonRoot: PropTypes.string.isRequired,
    reactGridLayout: PropTypes.string.isRequired,
  }).isRequired,
};

export default withStyles(styles)(DashboardLayout);
