import React from 'react';
import { Route, Switch } from 'react-router-dom';
import DashboardLayout from './component/dashboardLayout/DashboardLayout';

import Project from './component/project/Project';

function AppRoute() {
  return (
    <>
      <Switch>
        <Route path="/old-dashboard">
          <DashboardLayout />
        </Route>
        <Route path="/project/:id">
          <Project />
        </Route>
        <Route path="/">
          <div>home</div>
        </Route>
      </Switch>
    </>
  );
}

export default AppRoute;
