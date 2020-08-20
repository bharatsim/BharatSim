import React, { useState } from 'react';
import CSVReader from 'react-csv-reader';
import ChartTypeValidator from './TypeValidator';

const papaparseOptions = {
  header: true,
  dynamicTyping: true,
  skipEmptyLines: true,
};

const FilePreview = () => {
  const [dataRow, setDataRow] = useState(null);
  const [isOpenValidator, toggleValidator] = useState(false);
  return (
    <>
      <CSVReader
        cssClass="csv-reader-input"
        label="Select Csv To Validate Data Types "
        onFileLoaded={(data) => {
          setDataRow(data[0]);
          toggleValidator(true);
        }}
        onError={(e) => console.log(e)}
        parserOptions={papaparseOptions}
        inputId="ObiWan"
        inputStyle={{ color: 'red' }}
      />
      <ChartTypeValidator
        dataRow={dataRow}
        toggleValidator={toggleValidator}
        open={isOpenValidator}
      />
    </>
  );
};

export default FilePreview;
