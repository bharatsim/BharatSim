import { useState } from 'react';

const useForm = (validators) => {
  const [values, setValues] = useState({});
  const [errors, setErrors] = useState({});

  const setValue = (key, value) => {
    setValues((prevState) => ({ ...prevState, [key]: value }));
  };

  const setError = (key, error) => {
    setErrors((prevState) => ({ ...prevState, [key]: error }));
  };

  const validateFieldAndSetErrorMessage = (key, value, validator) => {
    const errorMessage = validator(value);
    setError(key, errorMessage);
    return errorMessage;
  };

  const validateAndSetValue = (key, value) => {
    validateFieldAndSetErrorMessage(key, value, validators[key]);

    setValue(key, value);
  };

  const onSubmit = (submitCallback) => {
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
  };

  const shouldEnableSubmit = () => {
    const keys = Object.keys(validators);
    return keys.every((key) => errors[key] === '');
  };

  return {
    values,
    errors,
    setError,
    validateAndSetValue,
    shouldEnableSubmit,
    onSubmit,
  };
};

export default useForm;
