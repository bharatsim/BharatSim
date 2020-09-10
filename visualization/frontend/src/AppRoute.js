import React from 'react';
import { Route, Switch } from 'react-router-dom';
import DashboardLayout from './component/dashboardLayout/DashboardLayout';

function AppRoute() {
  return (
    <>
      <Switch>
        <Route path="/old-dashboard">
          <DashboardLayout />
        </Route>
        <Route path="/project/:id">
          <div>Project with id</div>
        </Route>
        <Route path="/">
          <div>home</div>
        </Route>
      </Switch>
    </>
  );
}

export default AppRoute;
