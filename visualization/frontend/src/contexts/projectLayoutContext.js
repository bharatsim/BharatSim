import React from 'react';

const projectLayoutContext = React.createContext({
  projectMetadata: {
    name:'',
  },
  selectedDashboardMetadata: {
    name: '',
  },
});

const ProjectLayoutProvider = projectLayoutContext.Provider;

export { projectLayoutContext, ProjectLayoutProvider };
