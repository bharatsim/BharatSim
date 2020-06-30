import React from 'react';
import {
    Button,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    InputLabel,
    Select,
    MenuItem,
    Box
} from '@material-ui/core';
import useFetch from "../../hook/useFetch";
import {url} from "../../utils/url";
import labels from '../../constants/labels'

function renderMenuItems(items) {
   return items && items.map((item) => <MenuItem key={item} id={item} value={item}>{item}</MenuItem>)
}

export default function ChartConfigModal({open, onCancel, onOk}) {

    const [config, setConfig] = React.useState({});
    const csvHeaders = useFetch({url: url.HEADERS}) || {}

    const handleXChange = (event) => {
        setConfig({xColumn: event.target.value, yColumn: config.yColumn})
    };
    const handleYChange = (event) => {
        setConfig({yColumn: event.target.value, xColumn: config.xColumn})
    };

    const handleOk = () => {
        onOk(config)
    };

    return (
        <Dialog open={open} onClose={onCancel} aria-labelledby="form-dialog-title">
            <DialogTitle id="form-dialog-title">Chart Config</DialogTitle>
            <DialogContent>
               <Box display="flex" flexDirection="row">
                <InputLabel id="select-x-label">{labels.chartConfigModal.SELECT_X_COLUMN}</InputLabel>
                <Select
                    labelId="select-x-label"
                    id="select-x"
                    data-testid="select-x"
                    onChange={handleXChange}
                    variant='outlined'
                >
                    {renderMenuItems(csvHeaders.headers)}
                </Select>
               </Box>
                <Box display="flex" flexDirection="row">
                <InputLabel id="select-y-label">{labels.chartConfigModal.SELECT_Y_COLUMN}</InputLabel>
                <Select
                    labelId="select-y-label"
                    id="select-y"
                    variant='outlined'
                    data-testid="select-y"
                    onChange={handleYChange}
                >
                    {renderMenuItems(csvHeaders.headers)}
                </Select>
                </Box>


            </DialogContent>
            <DialogActions>
                <Button onClick={onCancel} color="primary">
                    Cancel
                </Button>
                <Button onClick={handleOk} color="primary">
                    Ok
                </Button>
            </DialogActions>
        </Dialog>
    );
}
