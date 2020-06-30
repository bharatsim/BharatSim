import React, { useState } from 'react';
import { Box, Button } from '@material-ui/core';
import ReactGridLayout, { WidthProvider } from 'react-grid-layout';

import 'react-grid-layout/css/styles.css';
import 'react-resizable/css/styles.css';
import './index.css';

import { createElement, getInitialLayout, getNewWidgetLayout } from './utils';
import labels from '../../constants/labels';
import ChartConfigModal from '../chartConfigModal/ChartConfigModal';
import useModal from '../../hook/useModal';

const GridLayout = WidthProvider(ReactGridLayout);
const cols = 12;

const DashboardLayout = () => {
  const [widgets, setWidgets] = useState(getInitialLayout());
  const [count, setCount] = useState(1);
  const [layout, setLayout] = useState();

  const { isOpen, closeModal, openModal } = useModal();

  const addItem = (config) => {
    setWidgets((prevWidgets) => {
      return prevWidgets.concat({
        ...getNewWidgetLayout(prevWidgets.length, cols, count),
        config,
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

  return (
    <Box pl={10} pt={10} pr={10}>
      <Box pb={2}>
        <Button onClick={openModal} variant="contained" color="primary">
          {labels.dashboardLayout.ADD_WIDGET}
        </Button>
      </Box>
      <GridLayout layout={layout} style={{ background: 'gray' }} onLayoutChange={onLayoutChange}>
        {widgets.map((item) => {
          return createElement(item);
        })}
      </GridLayout>
      <ChartConfigModal onCancel={closeModal} onOk={handleModalOk} open={isOpen} />
    </Box>
  );
};

export default DashboardLayout;
