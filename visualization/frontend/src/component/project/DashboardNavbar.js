import Tab from '@material-ui/core/Tab';
import Tabs from '@material-ui/core/Tabs';
import React from 'react';
import Box from '@material-ui/core/Box';
import { makeStyles } from '@material-ui/core/styles';
import dashboardIcon from '../../assets/images/dashboard-icon.svg';

const tabStyles = makeStyles((theme) => ({
  wrapper: {
    display: 'flex',
    flexDirection: 'row',
    '& > *:first-child': {
      marginBottom: '0 !important',
    },
    justifyContent: 'left',
  },
  labelIcon: {
    minHeight: '40px',
    height: '40px',
  },
  selected: {
    background: 'rgba(255, 255, 255,0.2)',
  },
  root: {
    padding: theme.spacing(3, 8, 3, 8),
    height: theme.spacing(10),
    borderRadius: theme.spacing(1),
  },
}));

const tabsStyles = makeStyles(() => ({
  indicator: {
    background: 'transparent',
  },
}));

export default function DashboardNavbar({ controllers, handleChange, value }) {
  const tabClasses = tabStyles();
  const tabsClasses = tabsStyles();
  return (
    <Box px={2}>
      <Tabs
        orientation="vertical"
        variant="fullWidth"
        value={value}
        onChange={handleChange}
        classes={tabsClasses}
      >
        {controllers.map((controller) => (
          <Tab
            icon={
              <Box pr={3} display="flex" alignItems="center">
                <img src={dashboardIcon} alt="dashboard-logo" />
              </Box>
            }
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
