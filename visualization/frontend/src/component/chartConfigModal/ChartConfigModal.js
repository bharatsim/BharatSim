import React from 'react';
import {Button, Dialog, DialogActions, DialogContent, DialogTitle} from '@material-ui/core';
import useFetch from "../../hook/useFetch";
import {url} from "../../utils/url";
import Dropdown from "../../uiComponent/Dropdown";
import {updateState} from "../../utils/helper";

export default function ChartConfigModal({open, onCancel, onOk}) {

  const [config, setConfig] = React.useState({});
  const csvHeaders = useFetch({url: url.HEADERS}) || {}

  const handleXChange = (value) => {
    setConfig((prevState)=>updateState(prevState, {xColumn: value}));
  };
  const handleYChange = (value) => {
    setConfig((prevState)=>updateState(prevState, {yColumn: value}))
  };

  const handleOk = () => {
    onOk(config)
  };

  return (
    <Dialog open={open} onClose={onCancel} aria-labelledby="form-dialog-title">
      <DialogTitle id="form-dialog-title">Chart Config</DialogTitle>
      <DialogContent>
        <Dropdown options={csvHeaders.headers} onChange={handleXChange} id="dropdown-x" label="select x axis"/>
        <Dropdown options={csvHeaders.headers} onChange={handleYChange} id="dropdown-y" label="select y axis"/>
      </DialogContent>
      <DialogActions>
        <Button onClick={onCancel} variant="contained" color="secondary">
          Cancel
        </Button>
        <Button onClick={handleOk} variant="contained" color="primary">
          Ok
        </Button>
      </DialogActions>
    </Dialog>
  );
}
