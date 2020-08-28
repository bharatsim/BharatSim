class InvalidInputException extends Error {
  constructor(message) {
    super(message);
    this.message = `Invalid Input - ${message}`;
    Object.setPrototypeOf(this, InvalidInputException.prototype);
  }
}

module.exports = InvalidInputException;
