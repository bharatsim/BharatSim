import React from 'react';

const projectLayoutContext = React.createContext({
  projectMetadata: {},
  selectedDashboardMetadata: {},
});

const ProjectLayoutProvider = projectLayoutContext.Provider;

export { projectLayoutContext, ProjectLayoutProvider };
