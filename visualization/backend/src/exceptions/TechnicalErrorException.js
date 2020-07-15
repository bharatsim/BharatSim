function technicalErrorException(err, res) {
  res.status(500).send({ errorMessage: `Technical error ${err.message}` });
}

module.exports = technicalErrorException;
