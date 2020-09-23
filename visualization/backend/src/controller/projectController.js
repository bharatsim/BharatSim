const router = require('express').Router();
const InvalidInputException = require('../exceptions/InvalidInputException');
const technicalErrorException = require('../exceptions/TechnicalErrorException');
const { addNewProject, getAllProjects } = require('../services/projectService');

router.post('/', async function (req, res) {
  const { projectData } = req.body;
  addNewProject(projectData)
    .then((projectId) => {
      res.send(projectId);
    })
    .catch((err) => {
      if (err instanceof InvalidInputException) {
        res.status(400).send({ errorMessage: err.message });
      } else {
        technicalErrorException(err, res);
      }
    });
});

router.get('/', async function (req, res) {
  getAllProjects()
    .then((projects) => {
      res.send(projects);
    })
    .catch((err) => {
      technicalErrorException(err, res);
    });
});

module.exports = router;
