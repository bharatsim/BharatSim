function updateState(prevState, updatedValue) {
  return { ...prevState, ...updatedValue };
}

function convertStringArrayToOptions(stringsArray) {
  return stringsArray.map((stringElement) => ({
    value: stringElement,
    displayName: stringElement,
  }));
}

function convertObjectArrayToOptions(objectArray, valueKey, displayNameKey) {
  return objectArray.map((objectElement) => ({
    value: objectElement[valueKey],
    displayName: objectElement[displayNameKey],
  }));
}

export { updateState, convertStringArrayToOptions, convertObjectArrayToOptions };
