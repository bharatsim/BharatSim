import React from 'react';
import Box from '@material-ui/core/Box';
import { makeStyles } from '@material-ui/core/styles';
import Grid from '@material-ui/core/Grid';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import Button from '@material-ui/core/Button';
import { useHistory } from 'react-router-dom';
import ProjectMetadataCard from './ProjectMetadataCard';
import theme from '../../theme/theme';
import TabPanel from '../../uiComponent/TabPanel';
import ButtonGroup from '../../uiComponent/ButtonGroup';

const styles = makeStyles(() => ({
  projectListContainer: {
    marginTop: theme.spacing(11),
  },
}));

function ExistingUserHomeScreen({ recentProjects }) {
  const [selectedTab] = React.useState(0);
  const history = useHistory();

  const classes = styles();

  function openProject(id) {
    history.push(`/project/${id}`);
  }

  function createNewProject() {
    history.push('/project/createNew');
  }

  return (
    <Box>
      <Box display="flex" justifyContent="space-between">
        <Tabs value={selectedTab} indicatorColor="primary" aria-label="disabled tabs example">
          <Tab label="Recent Projects" />
        </Tabs>
        <ButtonGroup>
          <Button variant="contained" size="small" onClick={createNewProject}>
            Add New
          </Button>
        </ButtonGroup>
      </Box>

      <Box className={classes.projectListContainer}>
        <TabPanel value={selectedTab} index={0}>
          {recentProjects && (
            <Grid container spacing={8} xl={12}>
              {recentProjects.map((project) => {
                const { _id } = project;
                return (
                  <Grid item xs={3} key={_id}>
                    <ProjectMetadataCard project={project} onProjectClick={openProject} />
                  </Grid>
                );
              })}
            </Grid>
          )}
        </TabPanel>
      </Box>
    </Box>
  );
}

export default ExistingUserHomeScreen;
