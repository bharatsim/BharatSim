class DatasourceNotFoundException extends Error {
  constructor(dataSourceName) {
    super(`datasource with id ${dataSourceName} not found`);
    this.message = `datasource with id ${dataSourceName} not found`;
    Object.setPrototypeOf(this, DatasourceNotFoundException.prototype);
  }
}

module.exports = DatasourceNotFoundException;
