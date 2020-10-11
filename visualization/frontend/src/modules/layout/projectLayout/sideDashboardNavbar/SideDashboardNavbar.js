import Tab from '@material-ui/core/Tab';
import Tabs from '@material-ui/core/Tabs';
import React from 'react';
import Box from '@material-ui/core/Box';
import dashboardIcon from '../../../../assets/images/dashboard-icon.svg';
import { useTabsStyles, useTabStyles } from './sideDashboardNavbarCSS';

/* TODO Add onchange for selecting dashboard */

export default function SideDashboardNavbar({ navItems, value }) {
  const tabClasses = useTabStyles();
  const tabsClasses = useTabsStyles();

  return (
    <Box px={2}>
      <Tabs orientation="vertical" variant="fullWidth" value={value} classes={tabsClasses}>
        {navItems.map((controller) => (
          <Tab
            icon={(
              <Box pr={3} display="flex" alignItems="center">
                <img src={dashboardIcon} alt="dashboard-logo" />
              </Box>
            )}
            label={controller}
            key={`controller-${controller}`}
            classes={tabClasses}
            data-testid={`tab-${controller}`}
          />
        ))}
      </Tabs>
    </Box>
  );
}
