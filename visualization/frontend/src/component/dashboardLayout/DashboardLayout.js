import React from 'react';
import ReactGridLayout, {WidthProvider} from "react-grid-layout";

import 'react-grid-layout/css/styles.css'
import 'react-resizable/css/styles.css'
import './index.css'

import Text from "../text/Text";

const GridLayout = WidthProvider(ReactGridLayout);

const layout = [
  {i: 'a', x: 0, y: 0, w: 2, h: 2}
]

const DashboardLayout = () => {
  return (
    <GridLayout layout={layout} style={{background:'gray'}}>
      <div key="a"><Text /></div>
    </GridLayout>
  )
}

export default DashboardLayout;