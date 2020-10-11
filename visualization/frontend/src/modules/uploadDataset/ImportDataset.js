import React from 'react';
import PropTypes from 'prop-types';

import Box from '@material-ui/core/Box';
import { makeStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import fileImportIcon from '../../assets/images/importFileIcon.svg';
import { parseCsv } from '../../utils/fileUploadUtils';
import LoaderOrError from '../../component/loaderOrError/LoaderOrError';
import useLoader, { loaderStates } from '../../hook/useLoader';

const useStyles = makeStyles((theme) => {
  return {
    importDataContainer: {
      display: 'flex',
      flexDirection: 'column',
      justifyContent: 'center',
      alignItems: 'center',
    },
    textContainer: {
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      marginTop: theme.spacing(5),
    },
  };
});

function ImportDataset({ setFile, handleNext, setPreviewData, setErrorStep }) {
  const classes = useStyles();
  const { loadingState, stopLoaderAfterSuccess, stopLoaderAfterError, startLoader } = useLoader(
    loaderStates.SUCCESS,
  );

  function onParse(csvData) {
    const { data, errors } = csvData;
    setPreviewData(data);
    if (errors.length > 0) {
      stopLoaderAfterError('unable to parse dataset');
      setErrorStep(0);
      return;
    }
    stopLoaderAfterSuccess();
    handleNext();
  }

  function handleFileImport(event) {
    startLoader();
    const selectedFile = event.target.files[0];
    parseCsv(selectedFile, onParse);
    setFile(selectedFile);
  }

  return (
    <LoaderOrError loadingState={loadingState.state}>
      <Box mb={8} mx={14} py={8} className={classes.importDataContainer}>
        <img src={fileImportIcon} alt="Import File" />
        <Box className={classes.textContainer}>
          <Typography>Drag your file here or </Typography>
          <Box ml={3}>
            <Button variant="outlined" component="label">
              Browse
              <input
                data-testid="file-input"
                type="file"
                style={{ display: 'none' }}
                onChange={handleFileImport}
              />
            </Button>
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
};

export default ImportDataset;
