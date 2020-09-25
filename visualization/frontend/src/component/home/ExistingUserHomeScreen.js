import React from 'react';
import Box from '@material-ui/core/Box';
import { makeStyles } from '@material-ui/core/styles';
import Grid from '@material-ui/core/Grid';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import Button from '@material-ui/core/Button';
import useFetch from '../../hook/useFetch';
import { api } from '../../utils/api';
import ProjectMetadataCard from './ProjectMetadataCard';
import theme from '../../theme/theme';
import TabPanel from '../../uiComponent/TabPanel';
import ButtonGroup from '../../uiComponent/ButtonGroup';

const styles = makeStyles(() => ({
  projectListContainer: {
    marginTop: theme.spacing(11),
  },
}));

function ExistingUserHomeScreen() {
  const { data: allProjects } = useFetch(api.fetchProjects);
  const [selectedTab, setSelectedTab] = React.useState(0);

  const classes = styles();
  const handleChange = (event, newValue) => {
    setSelectedTab(newValue);
  };

  return (
    <Box pt={22}>
      <Box display="flex" justifyContent="space-between">
        <Tabs
          value={selectedTab}
          indicatorColor="primary"
          onChange={handleChange}
          aria-label="disabled tabs example"
        >
          <Tab label="Recent Projects" />
        </Tabs>
        <ButtonGroup>
          <Button variant="contained" size="small">
            Add New
          </Button>
        </ButtonGroup>
      </Box>

      <Box className={classes.projectListContainer}>
        <TabPanel value={selectedTab} index={0}>
          {allProjects && (
            <Grid container spacing={8} xl={12}>
              {allProjects.projects.map((project) => (
                <Grid item xs={3}>
                  <ProjectMetadataCard name={project.name} />
                </Grid>
              ))}
            </Grid>
          )}
        </TabPanel>
      </Box>
    </Box>
  );
}

export default ExistingUserHomeScreen;
