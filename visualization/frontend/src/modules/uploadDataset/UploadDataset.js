import React, { useContext, useState } from 'react';
import { useHistory } from 'react-router-dom';
import { useSnackbar } from 'notistack';

import { Button, Typography, Box, makeStyles, Step, Stepper, StepLabel } from '@material-ui/core';

import ProjectHeader from '../../uiComponent/ProjectHeader';
import ButtonGroup from '../../uiComponent/ButtonGroup';

import ConfigureDatatype from './ConfigureDatatype';
import ImportDataset from './ImportDataset';
import { projectLayoutContext } from '../../contexts/projectLayoutContext';
import { api } from '../../utils/api';
import useFetchExecutor from '../../hook/useFetchExecuter';
import LoaderOrError from '../../component/loaderOrError/LoaderOrError';

const useStyles = makeStyles((theme) => {
  return {
    configureProjectDataBar: {
      width: '100%',
      display: 'flex',
      justifyContent: 'space-between',
      backgroundColor: `${theme.colors.grayScale['100']}80`,
      alignItems: 'center',
      padding: theme.spacing(3, 8),
    },
    contentWrapper: {
      backgroundColor: '#FFFFFF',
      border: '1px solid',
      borderColor: `${theme.colors.primaryColorScale['500']}3d`,
      borderRadius: theme.spacing(1),
      margin: theme.spacing(8),
      padding: theme.spacing(8, 10),
    },
  };
});

function getStepContent(
  stepIndex,
  setFile,
  setSchema,
  file,
  schema,
  handleNext,
  setPreviewData,
  setErrorStep,
  previewData,
) {
  if (stepIndex === 0) {
    return (
      <ImportDataset
        setFile={setFile}
        handleNext={handleNext}
        setPreviewData={setPreviewData}
        setErrorStep={setErrorStep}
        setSchema={setSchema}
      />
    );
  }

  if (stepIndex === 1) {
    return (
      <ConfigureDatatype
        schema={schema}
        selectedFile={file}
        handleNext={handleNext}
        previewData={previewData}
      />
    );
  }

  return (
    <ConfigureDatatype
      schema={schema}
      selectedFile={file}
      handleNext={handleNext}
      previewData={previewData}
    />
  );
}

function UploadDataset() {
  const classes = useStyles();
  const history = useHistory();
  const {
    projectMetadata,
    selectedDashboardMetadata: { _id: selectedDashboardId, name: selectedDashboardName },
  } = useContext(projectLayoutContext);

  const [activeStep, setActiveStep] = useState(0);
  const [errorStep, setErrorStep] = useState(undefined);
  const steps = ['Import Data', 'Configure Datatype', 'Upload to Dashboard'];
  const [file, setFile] = useState();
  const [schema, setSchema] = useState();
  const [previewData, setPreviewData] = useState();
  const { executeFetch, loadingState } = useFetchExecutor();
  const { enqueueSnackbar } = useSnackbar();

  function handleNext() {
    setActiveStep((prevActiveStep) => prevActiveStep + 1);
  }

  async function onClickUploadAndSave() {
    handleNext();
    await executeFetch(api.uploadFileAndSchema, [
      { file, schema, dashboardId: selectedDashboardId },
    ]).then(() => {
      history.push(`configure-dataset`);
      enqueueSnackbar(`uploaded ${file.name} to dashboard ${selectedDashboardName}`, {
        variant: 'success',
      });
    });
  }

  function onCancel() {
    history.push(`configure-dataset`);
  }

  return (
    <Box>
      <ProjectHeader>{projectMetadata.name}</ProjectHeader>
      <Box className={classes.configureProjectDataBar}>
        <Typography variant="h6"> Configure Dashboard Data :: Upload Dataset</Typography>
        <ButtonGroup>
          <Button variant="text" onClick={onCancel} size="small">
            Cancel
          </Button>
          <Button
            variant="contained"
            color="primary"
            size="small"
            disabled={activeStep !== 1}
            onClick={onClickUploadAndSave}
          >
            Upload
          </Button>
        </ButtonGroup>
      </Box>
      <Box className={classes.contentWrapper}>
        <Box px={8} pb={8}>
          <Stepper activeStep={activeStep}>
            {steps.map((label, index) => (
              <Step key={label}>
                <StepLabel error={errorStep === index}>{label}</StepLabel>
              </Step>
            ))}
          </Stepper>
        </Box>
        <Box>
          <LoaderOrError loadingState={loadingState}>
            {getStepContent(
              activeStep,
              setFile,
              setSchema,
              file,
              schema,
              handleNext,
              setPreviewData,
              setErrorStep,
              previewData,
            )}
          </LoaderOrError>
        </Box>
      </Box>
    </Box>
  );
}

export default UploadDataset;
