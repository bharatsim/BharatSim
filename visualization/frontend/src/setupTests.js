import '@testing-library/jest-dom/extend-expect';
import 'jest-canvas-mock';

global.mockPropsCapture = (props) => {
  return Object.entries(props).map(([key, value]) => {
    let actualValue;
    switch (typeof value) {
      case 'string':
        actualValue = value;
        break;
      case 'function':
        actualValue = `function ${key}`;
        break;
      case 'object':
        actualValue = value.toString();
        break;
      default:
        actualValue = value;
    }
    return `${key} = ${actualValue}`;
  });
};
