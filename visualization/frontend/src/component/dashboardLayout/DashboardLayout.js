import React, { useState } from 'react';
import { Box, Button } from '@material-ui/core';
import ReactGridLayout, { WidthProvider } from 'react-grid-layout';

import 'react-grid-layout/css/styles.css';
import 'react-resizable/css/styles.css';
import './index.css';

import { renderElement, getNewWidgetLayout } from './utils';
import labels from '../../constants/labels';
import ChartConfigModal from '../chartConfigModal/ChartConfigModal';
import useModal from '../../hook/useModal';
import FileUpload from '../../uiComponent/FileUpload';
import { chartTypes } from '../../constants/charts';

const GridLayout = WidthProvider(ReactGridLayout);
const cols = 12;

const DashboardLayout = () => {
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
      </Box>
      <GridLayout
        layout={layout}
        style={{ background: 'gray', minHeight: '600px' }}
        onLayoutChange={onLayoutChange}
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

export default DashboardLayout;
