const { useState } = require('react');

function useFrom(defaultValues = {}, validators = {}) {
  const [values, setValues] = useState(defaultValues);
  const [errors, setErrors] = useState({});

  function setValue(id, value) {
    setValues((prevState) => ({ ...prevState, [id]: value }));
  }
  function setError(id, error) {
    setErrors((prevState) => ({ ...prevState, [id]: error }));
  }
  function validate(id, value) {
    return validators[id] ? validators[id](value) : '';
  }
  function handleInputChange(id, value) {
    const error = validate(id, value);
    setError(id, error);
    setValue(id, value);
  }
  function shouldEnableSubmit() {
    const keys = Object.keys(validators);
    return keys.every((key) => errors[key] === '');
  }
  return { values, errors, handleInputChange, shouldEnableSubmit };
}

export default useFrom;
