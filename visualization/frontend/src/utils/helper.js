function updateState(prevState, updatedValue) {
  return { ...prevState, ...updatedValue };
}

export { updateState };
