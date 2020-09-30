import React from 'react';
import Box from '@material-ui/core/Box';
import TabPanel from '../../uiComponent/TabPanel';
import ConfigureDashboardData from '../project/ConfigureDashboardData';

export default function DashboardView({ projectName, dashboardData, value }) {
  return (
    <Box>
      {dashboardData.map((dashboard, index) => (
        <TabPanel value={value} index={index} key={`value-${dashboard.name}`}>
          <ConfigureDashboardData dashboardData={dashboard} projectName={projectName} />
        </TabPanel>
      ))}
    </Box>
  );
}
