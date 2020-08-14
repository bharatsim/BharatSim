function updateState(prevState, updatedValue) {
  return { ...prevState, ...updatedValue };
}

function convertStringArrayToOptions(stringsArray) {
  return stringsArray.map((stringElement) => ({
    value: stringElement,
    displayName: stringElement,
  }));
}

function convertObjectArrayToOptionStructure(objectArray, displayNameKey, valueKey) {
  return objectArray.map((objectElement) => ({
    value: valueKey ? objectElement[valueKey] : objectElement,
    displayName: objectElement[displayNameKey],
  }));
}

export { updateState, convertStringArrayToOptions, convertObjectArrayToOptionStructure };
