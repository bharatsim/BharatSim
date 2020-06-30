function updateState(prevState,updatedValue){
  return Object.assign({},prevState,updatedValue)
}

export {
  updateState,
}