async function getData(datasourceModel, columnsMap) {
  return datasourceModel.find({}, { _id: 0, ...columnsMap }).then((data) => data);
}

async function insert(datasourceModel, data) {
  return datasourceModel.insertMany(data);
}

module.exports = {
  getData,
  insert,
};
