import Box from '@material-ui/core/Box';
import { Typography } from '@material-ui/core';
import React, { useContext, useState } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Step from '@material-ui/core/Step';
import StepLabel from '@material-ui/core/StepLabel';
import Stepper from '@material-ui/core/Stepper';
import ImportDataset from './ImportDataset';
import ConfigureDatatype from './ConfigureDatatype';
import ProjectHeader from '../../uiComponent/ProjectHeader';
import { projectLayoutContext } from '../../contexts/projectLayoutContext';
import Modal from '../../uiComponent/Modal';
import CircularProgress from '@material-ui/core/CircularProgress';

const useStyles = makeStyles((theme) => {
  return {
    configureProjectDataBar: {
      width: '100%',
      display: 'flex',
      justifyContent: 'space-between',
      backgroundColor: theme.colors.grayScale['100'],
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
  return {
    0: (
      <ImportDataset
        setFile={setFile}
        handleNext={handleNext}
        setPreviewData={setPreviewData}
        setErrorStep={setErrorStep}
      />
    ),
    1: (
      <ConfigureDatatype
        onSchemaSelect={setSchema}
        selectedFile={file}
        handleNext={handleNext}
        previewData={previewData}
      />
    ),
  }[stepIndex];
}

function UploadDataset() {
  const classes = useStyles();
  const { projectMetadata } = useContext(projectLayoutContext);
  const [activeStep, setActiveStep] = useState(0);
  const [errorStep, setErrorStep] = useState(undefined);
  const steps = ['Import Data', 'Configure Datatype', 'Upload to Dashboard'];
  const [file, setFile] = useState();
  const [schema, setSchema] = useState();
  const [, setPreviewData] = useState();

  function handleNext() {
    setActiveStep((prevActiveStep) => prevActiveStep + 1);
  }

  return (
    <Box>
      <ProjectHeader>{projectMetadata.name}</ProjectHeader>
      <Box className={classes.configureProjectDataBar}>
        <Typography variant="h6"> Configure Dashboard Data :: Upload Dataset</Typography>
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
          {getStepContent(
            activeStep,
            setFile,
            setSchema,
            file,
            schema,
            handleNext,
            setPreviewData,
            setErrorStep,
          )}
        </Box>
      </Box>
    </Box>
  );
}

export default UploadDataset;
