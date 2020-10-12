import React from 'react';
import { Route, Switch } from 'react-router-dom';
import DashboardLayout from './component/dashboardLayout/DashboardLayout';
import Home from './modules/home/Home';
import ConfigureDataset from './modules/configureDataset/ConfigureDataset';
import UploadDataset from './modules/uploadDataset/UploadDataset';
import ProjectLayout from './modules/layout/projectLayout/projectLayout/ProjectLayout';
import ProjectHomeScreen from './modules/projectHomeScreen/ProjectHomeScreen';

function renderProjectRoute() {
  return (
    <ProjectLayout>
      <Switch>
        <Route exact path="/projects/:id/upload-dataset">
          <UploadDataset />
        </Route>
        <Route exact path="/projects/:id">
          <ConfigureDataset />
        </Route>
      </Switch>
    </ProjectLayout>
  );
}

function AppRoute() {
  return (
    <>
      <Switch>
        <Route path="/old-dashboard">
          <DashboardLayout />
        </Route>
        <Route path="/projects/create">
          <ProjectLayout>
            <ProjectHomeScreen />
          </ProjectLayout>
        </Route>
        <Route path="/projects/:id">{renderProjectRoute()}</Route>
        <Route path="/">
          <Home />
        </Route>
      </Switch>
    </>
  );
}

export default AppRoute;
