class ColumnsNotFoundException extends Error {
  constructor() {
    super(`One or more columns not found`);
    this.message = `One or more columns not found`;
    Object.setPrototypeOf(this, ColumnsNotFoundException.prototype);
  }
}

module.exports = ColumnsNotFoundException;
