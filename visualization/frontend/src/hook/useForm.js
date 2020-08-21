import { useState } from 'react';

function useForm(validators) {
  const [values, setValues] = useState({});
  const [errors, setErrors] = useState({});

  function setValue(key, value) {
    setValues((prevState) => ({ ...prevState, [key]: value }));
  }

  function setError(key, error) {
    setErrors((prevState) => ({ ...prevState, [key]: error }));
  }

  function validateFieldAndSetErrorMessage(key, value, validator) {
    const errorMessage = validator(value);
    setError(key, errorMessage);
    return errorMessage;
  }

  function validateAndSetValue(key, value) {
    validateFieldAndSetErrorMessage(key, value, validators[key]);

    setValue(key, value);
  }

  function onSubmit(submitCallback) {
    const validatorKeys = Object.keys(validators);
    const isFormValid = validatorKeys.every((validatorKey) => {
      return (
        validateFieldAndSetErrorMessage(
          validatorKey,
          values[validatorKey],
          validators[validatorKey],
        ) === ''
      );
    });

    if (isFormValid) submitCallback(values);
  }

  function shouldEnableSubmit() {
    const keys = Object.keys(validators);
    return keys.every((key) => errors[key] === '' && !!values[key]);
  }

  function resetField(key) {
    setValue(key, undefined);
  }

  function resetFields(keys) {
    keys.map((key) => resetField(key));
  }

  return {
    values,
    errors,
    setError,
    validateAndSetValue,
    shouldEnableSubmit,
    onSubmit,
    resetFields,
  };
}

export default useForm;
