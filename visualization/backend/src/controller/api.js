const router = require('express').Router();
const csvService = require ('../services/csvService.js')


router.get('/data', function (req, res) {
    const {columns} = req.query;
    res.json(csvService.getData(columns))
})

router.get('/headers', function (req, res) {
    res.json(csvService.getHeaders())
})
module.exports=router;