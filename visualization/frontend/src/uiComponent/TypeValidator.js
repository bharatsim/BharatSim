import React, { useState } from 'react';

import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle } from '@material-ui/core';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';

const createSchema = (row) => {
  return Object.keys(row).reduce((acc, element) => {
    acc[element] = typeof row[element];
    return acc;
  }, {});
};
function availableValueDropdown(){
  return (
    <FormControl className={classes.formControl}>
      <InputLabel id="demo-simple-select-label">Age</InputLabel>
      <Select
        labelId="demo-simple-select-label"
        id="demo-simple-select"
        value={age}
        onChange={handleChange}
      >
        <MenuItem value={10}>Ten</MenuItem>
        <MenuItem value={20}>Twenty</MenuItem>
        <MenuItem value={30}>Thirty</MenuItem>
      </Select>
    </FormControl>
)
  
}
function createCell(value) {
  return <TableCell key={value}>{value}</TableCell>
}
function createTypesTable(schema){
  return  (
    <TableContainer component={Paper}>
      <Table size="small" aria-label="a dense table">
        <TableHead>
          <TableRow>
            {Object.keys(schema).map(e=> createCell(e))}
          </TableRow>
        </TableHead>
        <TableBody>
          <TableRow>
            {Object.values(schema).map(e=> createCell(e))}
          </TableRow>
        </TableBody>
      </Table>
    </TableContainer>
)
    }


function ChartTypeValidator({ open, toggleValidator, dataRow }) {
  const [schema, setSchema] = useState(null);
  return (
    <Dialog
      open={open}
      onClose={() => {
        toggleValidator(false);
      }}
      aria-labelledby="form-dialog-title"
    >
      <DialogTitle id="form-dialog-title">Chart Data Types</DialogTitle>

      <>
        <DialogContent>
          {dataRow ? setSchema(createSchema(dataRow)) : setSchema(undefined)}
          <Box>{ schema ?  createTypesTable(schema):"Data Not Present" }</Box>
        </DialogContent>
        <DialogActions>
          <Button
            onClick={() => {
              toggleValidator(false);
            }}
            variant="contained"
            color="secondary"
          >
            Cancel
          </Button>
          <Button
            onClick={() => {
                toggleValidator(false);
              }}
            variant="contained"
            color="secondary"
          >
            Upload
          </Button>

        </DialogActions>
      </>
    </Dialog>
  );
}

export default ChartTypeValidator;
