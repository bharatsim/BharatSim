class InvalidInputException extends Error {
  constructor(message) {
    super(message);
    this.message = message;
    Object.setPrototypeOf(this, InvalidInputException.prototype);
  }
}

module.exports = InvalidInputException;
