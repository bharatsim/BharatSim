import React, { useEffect, useState } from "react";

import { useParams } from "react-router-dom";
import { Box, Typography } from "@material-ui/core";

import LoaderOrError from "../../../../component/loaderOrError/LoaderOrError";
import SideDashboardNavbar from "../sideDashboardNavbar/SideDashboardNavbar";

import useProjectLayoutStyle from "./projectLayoutCSS";
import useFetch from "../../../../hook/useFetch";

import { api } from "../../../../utils/api";
import { ProjectLayoutProvider } from "../../../../contexts/projectLayoutContext";
import { ChildrenPropTypes } from "../../../../commanPropTypes";

async function fetchProjectData(id) {
  if (id) {
    return api.getProject(id);
  }
  return null;
}

async function fetchDashboards(id) {
  if (id) {
    return api.getAllDashBoardByProjectId(id);
  }
  return null;
}

function ProjectLayout({ children }) {
  const classes = useProjectLayoutStyle();
  const { id } = useParams();

  const [projectMetadata, setProjectMetadata] = useState({
    id: undefined,
    name: 'untitled project',
  });

  const [dashboards, setDashboards] = useState([]);

  const [selectedDashboard] = useState(0);

  const {
    data: fetchedProjectMetadata,
    loadingState: projectLoadingState,
  } = useFetch(fetchProjectData, [id]);

  const { data: fetchedDashboards, loadingState: dashboardLoadingState } = useFetch(
    fetchDashboards,
    [id],
  );

  useEffect(() => {
    if (fetchedDashboards) setDashboards(fetchedDashboards.dashboards);
  }, [fetchedDashboards]);

  useEffect(() => {
    if (fetchedProjectMetadata) {
      const { _id, name } = fetchedProjectMetadata.project;
      setProjectMetadata({ id: _id, name });
    }
  }, [fetchedProjectMetadata]);

  return (
    <LoaderOrError loadingState={projectLoadingState}>
      <LoaderOrError loadingState={dashboardLoadingState}>
        <Box className={classes.layoutContainer}>
          <Box className={classes.sideBarLayout}>
            <SideDashboardNavbar
              navItems={dashboards.map((dashboard) => dashboard.name)}
              value={selectedDashboard}
            />
          </Box>
          <Box display="flex" flex={1} flexDirection="column">
            <Box className={classes.projectNameBar}>
              <Typography variant="h5">{projectMetadata.name}</Typography>
            </Box>
            <ProjectLayoutProvider
              value={{
                projectMetadata,
                selectedDashboardMetadata: dashboards[selectedDashboard] || {},
              }}
            >
              {children}
            </ProjectLayoutProvider>
          </Box>
        </Box>
      </LoaderOrError>
    </LoaderOrError>
  );
}

ProjectLayout.propTypes = {
  children: ChildrenPropTypes.isRequired,
};

export default ProjectLayout;
