import { render } from '@testing-library/react';
import React from 'react';
import { fireEvent } from '@testing-library/dom';
import InputTextField from '../InputTextField';
import withThemeProvider from '../../theme/withThemeProvider';

describe('InputTextField', () => {
  const InputTextFieldComponent = withThemeProvider(InputTextField);
  it('should render input text field with for given information', () => {
    const { container } = render(<InputTextFieldComponent
      onChange={jest.fn()}
      id="input-id"
      label="input-label"
      value="input-value"
      error=""
      helperText="helper-text"
    />);
    expect(container).toMatchSnapshot();
  });
  it('should change the value on change for input field', () => {
    const mockOnChange = jest.fn();
    const { getByLabelText } = render(<InputTextFieldComponent
      onChange={mockOnChange}
      id="input-id"
      label="input-label"
      value="input-value"
      error=""
      helperText="helper-text"
    />);
    fireEvent.change(getByLabelText('input-label'), {
      target: { value: 'new input value' },
    });
    expect(mockOnChange).toHaveBeenCalled();
  });
  it('should show error if any errors are passed in prop ', () => {
    const { getByText } = render(<InputTextFieldComponent
      onChange={jest.fn()}
      id="input-id"
      label="input-label"
      value="input-value"
      error="Some error has been created"
      helperText="helper-text"
    />);
    expect(getByText("Some error has been created")).not.toBe(null)
  });
});