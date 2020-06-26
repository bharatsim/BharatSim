import React, {useState} from 'react';
import {Box, Button} from "@material-ui/core";
import ReactGridLayout, {WidthProvider} from "react-grid-layout";

import 'react-grid-layout/css/styles.css'
import 'react-resizable/css/styles.css'
import './index.css'

import {createElement, getInitialLayout} from './utils'
import labels from '../../constants/labels'

const GridLayout = WidthProvider(ReactGridLayout);

const DashboardLayout = () => {
  const [items, setItems] = useState(getInitialLayout());
  const [count, setCount] = useState(0);
  const [cols, setCols] = useState();
  const [layout, setLayout] = useState();

  const onAddItem = () => {
    setItems((prevItems) => {
      return prevItems.concat({
        i: 'n' + count,
        x: (prevItems.length * 2) % (cols || 12),
        y: Infinity, // puts it at the bottom
        w: 2,
        h: 2,
      });
    });
    setCount((prevCount) => prevCount + 1);
  };

  const onLayoutChange = (layout) => {
    setLayout(layout);
  };

  return (
    <Box pl={10} pt={10} pr={10}>
      <Box pb={2}>
        <Button onClick={onAddItem} variant="contained" color="primary">{labels.dashboardLayout.ADD_WIDGET}</Button>
      </Box>
      <GridLayout layout={layout} style={{background: 'gray'}} onLayoutChange={onLayoutChange}>
        {items.map((item) => {
          return createElement(item);
        })}
      </GridLayout>
    </Box>
  )
}

export default DashboardLayout;