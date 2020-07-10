async function getData(datasourceModel, columnsMap) {
  return datasourceModel.find({}, { _id: 0, ...columnsMap }).then((data) => data);
}
module.exports = {
  getData,
};
