const express = require('express');
const apiRoutes = require('./src/controller/api.js');

const app = express()

const port = 3005

app.get('/', (req, res) => res.send('Hello World!'))

app.use('/api', apiRoutes)

app.listen(port, () => console.log(`Example app listening at http://localhost:${port}`))