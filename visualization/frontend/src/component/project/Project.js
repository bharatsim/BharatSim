import React from 'react';
import DashboardNavbar from './DashboardNavbar';
import SideBarLayout from '../layout/SideBarLayout';
import DashboardView from './DashboardView';

function Project() {
  const [selectedDashboard, setSelectedDashboard] = React.useState(0);

  const handleChange = (event, newValue) => {
    setSelectedDashboard(newValue);
  };

  const controllers = ['Dashboard 1'];
  const views = [{ name: 'dashboard1' }];
  return (
    <SideBarLayout
      ControllerComponent={() => (
        <DashboardNavbar
          controllers={controllers}
          value={selectedDashboard}
          handleChange={handleChange}
        />
      )}
      ViewComponent={() => <DashboardView views={views} value={selectedDashboard} />}
    />
  );
}

export default Project;
