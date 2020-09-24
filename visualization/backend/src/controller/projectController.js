const router = require('express').Router();
const InvalidInputException = require('../exceptions/InvalidInputException');
const technicalErrorException = require('../exceptions/TechnicalErrorException');
const { addNewProject, getAllProjects, getProject } = require('../services/projectService');

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

router.get('/:id', async function (req, res) {
  const { id: projectId } = req.params;
  getProject(projectId)
    .then((project) => {
      res.send(project);
    })
    .catch((err) => {
      technicalErrorException(err, res);
    });
});

module.exports = router;
