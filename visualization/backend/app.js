const express = require('express');
const apiRoutes = require('./src/controller/api.js');
require('./setupDB');

const app = express();

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

const port = 3005;

app.get('/', (req, res) => res.send('Hello World!'));

app.use('/api', apiRoutes);

// eslint-disable-next-line no-console
app.listen(port, () => console.log(`Example app listening at http://localhost:${port}`));
