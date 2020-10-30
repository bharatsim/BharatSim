import React, { useState } from 'react';
import PropTypes from 'prop-types';

import Box from '@material-ui/core/Box';
import { makeStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import fileImportIcon from '../../assets/images/importFileIcon.svg';
import { createSchema, parseCsv } from '../../utils/fileUploadUtils';
import LoaderOrError from '../../component/loaderOrError/LoaderOrError';
import useLoader, { loaderStates } from '../../hook/useLoader';
import ErrorBar from '../../uiComponent/ErrorBar';
import { validateFile } from '../../utils/validators';

const useStyles = makeStyles((theme) => {
  return {
    importDataContainer: {
      display: 'flex',
      flexDirection: 'column',
      justifyContent: 'center',
      alignItems: 'center',
      border: '1px dashed',
      borderRadius: theme.spacing(1),
      borderColor: theme.colors.primaryColorScale['200'],
    },
    textContainer: {
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      marginTop: theme.spacing(5),
    },
  };
});

function ImportDataset({ setFile, handleNext, setPreviewData, setErrorStep, setSchema }) {
  const classes = useStyles();
  const { loadingState, stopLoaderAfterSuccess, startLoader } = useLoader(loaderStates.SUCCESS);
  const [error, setError] = useState();

  function onParse(csvData) {
    const { data, errors } = csvData;
    if (errors.length > 0) {
      stopLoaderAfterSuccess();
      setError(
        'Failed to Import file due to parsing error. Please review the file and ensure that its a valid CSV file.',
      );
      setErrorStep(0);
      return;
    }
    setPreviewData(data);
    const schema = createSchema(data[0]);
    setSchema(schema);
    stopLoaderAfterSuccess();
    handleNext();
  }

  function handleFileImport(event) {
    setErrorStep(null);
    const selectedFile = event.target.files[0];
    const validationError = validateFile(selectedFile);
    if (validationError) {
      setError(validationError);
      setErrorStep(0);
      return;
    }
    startLoader();
    parseCsv(selectedFile, onParse);
    setFile(selectedFile);
  }

  return (
    <LoaderOrError loadingState={loadingState.state}>
      <Box mb={8} mx={14}>
        <ErrorBar visible={!!error} message={error} />
        <Box py={8} mt={6} className={classes.importDataContainer}>
          <img src={fileImportIcon} alt="Import File" />
          <Box className={classes.textContainer}>
            <Typography>Drag your file here or </Typography>
            <Box ml={3}>
              <Button variant="outlined" component="label" size="small">
                Browse
                <input
                  data-testid="file-input"
                  type="file"
                  accept=".csv"
                  style={{ display: 'none' }}
                  onChange={handleFileImport}
                />
              </Button>
            </Box>
          </Box>
        </Box>
      </Box>
    </LoaderOrError>
  );
}

ImportDataset.propTypes = {
  setFile: PropTypes.func.isRequired,
  handleNext: PropTypes.func.isRequired,
  setPreviewData: PropTypes.func.isRequired,
  setErrorStep: PropTypes.func.isRequired,
  setSchema: PropTypes.func.isRequired,
};

export default ImportDataset;
