import React from 'react';
import { Route, Switch } from 'react-router-dom';
import DashboardLayout from './component/dashboardLayout/DashboardLayout';
import Home from './modules/home/Home';
import ConfigureDashboardData from './modules/configureDataset/ConfigureDashboardData';
import UploadDataset from './modules/uploadDataset/UploadDataset';
import ProjectLayout from './modules/layout/projectLayout/projectLayout/ProjectLayout';

function AppRoute() {
  return (
    <>
      <Switch>
        <Route path="/old-dashboard">
          <DashboardLayout />
        </Route>
        <Route path="/projects/create">
          <ProjectLayout>
            <ConfigureDashboardData />
          </ProjectLayout>
        </Route>
        <Route path="/projects/:id">
          <ProjectLayout>
            <Switch>
              <Route exact path="/projects/:id/upload-dataset">
                <UploadDataset />
              </Route>
              <Route exact path="/projects/:id">
                <ConfigureDashboardData />
              </Route>
            </Switch>
          </ProjectLayout>
        </Route>
        <Route path="/">
          <Home />
        </Route>
      </Switch>
    </>
  );
}

export default AppRoute;
