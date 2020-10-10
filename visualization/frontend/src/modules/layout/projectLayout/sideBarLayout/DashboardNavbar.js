import Tab from '@material-ui/core/Tab';
import Tabs from '@material-ui/core/Tabs';
import React from 'react';
import Box from '@material-ui/core/Box';
import { makeStyles } from '@material-ui/core/styles';
import dashboardIcon from '../../../../assets/images/dashboard-icon.svg';

const tabStyles = makeStyles((theme) => ({
  root: {
    padding: theme.spacing(3, 7),
    height: theme.spacing(10),
    borderRadius: theme.spacing(1),
    minHeight: 'unset',
    minWidth: 'unset',
    boxSizing: 'border-box',
    ...theme.typography.subtitle2,
    [theme.breakpoints.up('sm')]: {
      minWidth: 'unset',
    },
  },
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
    background: theme.colors.primaryColorScale['500'],
  },
  textColorInherit: {
    border: 'none',
    color: theme.colors.textLight.primary,
    textTransform: 'capitalize',
    opacity: 'unset',
    '&$selected': {
      border: 'none',
      opacity: 'unset',
      color: theme.colors.textLight.primary,
    },
  },
}));

const tabsStyles = makeStyles(() => ({
  indicator: {
    background: 'transparent',
  },
}));

/* TODO Add onchange for selecting dashboard */

export default function DashboardNavbar({ navItems, value }) {
  const tabClasses = tabStyles();
  const tabsClasses = tabsStyles();
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
