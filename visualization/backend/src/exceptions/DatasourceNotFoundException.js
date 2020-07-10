class DatasourceNotFoundException extends Error {
  constructor(dataSourceName) {
    super(`datasource with name ${dataSourceName} not found`);
    this.message = `datasource with name ${dataSourceName} not found`;
    Object.setPrototypeOf(this, DatasourceNotFoundException.prototype);
  }
}

module.exports = DatasourceNotFoundException;
