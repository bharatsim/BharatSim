import React, {useState} from 'react';
import {Box, Button} from "@material-ui/core";
import ReactGridLayout, {WidthProvider} from "react-grid-layout";

import 'react-grid-layout/css/styles.css'
import 'react-resizable/css/styles.css'
import './index.css'

import {createElement, getInitialLayout} from './utils'
import labels from '../../constants/labels'
import ChartConfigModal from '../chartConfigModal/ChartConfigModal'

const GridLayout = WidthProvider(ReactGridLayout);
const cols = 12

const DashboardLayout = () => {
  const [items, setItems] = useState(getInitialLayout());
  const [count, setCount] = useState(0);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const [layout, setLayout] = useState();

  const addItem = (config) => {
    setItems((prevItems) => {
      return prevItems.concat({
        i: 'n' + count,
        x: (prevItems.length * 2) % (cols),
        y: Infinity, // puts it at the bottom
        w: 2,
        h: 2,
        config
      });
    });
    setCount((prevCount) => prevCount + 1);
  };

  const openModal = () =>{
    setIsModalOpen(true);
  }
  const closeModal = () =>{
    setIsModalOpen(false);
  }

  const handleModalOk = (config) =>{
    addItem(config)
    closeModal();
  }

  const onLayoutChange = (layout) => {
    setLayout(layout);
  };

  return (
    <Box pl={10} pt={10} pr={10}>
      <Box pb={2}>
        <Button onClick={openModal} variant="contained" color="primary">{labels.dashboardLayout.ADD_WIDGET}</Button>
      </Box>
      <GridLayout layout={layout} style={{background: 'gray'}} onLayoutChange={onLayoutChange}>
        {items.map((item) => {
          return createElement(item);
        })}
      </GridLayout>
      <ChartConfigModal onCancel={closeModal} onOk={handleModalOk} open={isModalOpen}/>
    </Box>
  )
}

export default DashboardLayout;