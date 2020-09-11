import React from 'react';
import Box from '@material-ui/core/Box';

function TabPanel(props) {
  const { children, value, index } = props;

  return <div>{value === index && <Box p={3}>{children}</Box>}</div>;
}

export default function DashboardView({ views, value }) {
  return (
    <Box>
      {views.map((view, index) => (
        <TabPanel value={value} index={index} key={`value-${view.name}`}>
          {view.name}
        </TabPanel>
      ))}
    </Box>
  );
}
