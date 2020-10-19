import React from 'react';

const projectLayoutContext = React.createContext({
  projectMetadata: {
    id: undefined,
    name: undefined,
  },
  selectedDashboardMetadata: {
    name: undefined,
    _id: undefined
  },
  addDashboard: null,
});

const ProjectLayoutProvider = projectLayoutContext.Provider;

export { projectLayoutContext, ProjectLayoutProvider };
