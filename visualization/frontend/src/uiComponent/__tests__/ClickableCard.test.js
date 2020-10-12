import React from 'react';
import { render } from '@testing-library/react';
import { fireEvent } from '@testing-library/dom';
import ClickableCard from '../ClickableCard';
import withThemeProvider from '../../theme/withThemeProvider';

describe('<ClickableCard />', () => {
  const ClickableCardComponent = withThemeProvider(ClickableCard);
  it('should match snapshot', () => {
    const { container } = render(
      <ClickableCardComponent onClick={jest.fn()}>Sample Test Component</ClickableCardComponent>,
    );
    expect(container).toMatchSnapshot();
  });
  it('should call onclick function on click of component', () => {
    const mockClickFunction = jest.fn();
    const { getByText } = render(
      <ClickableCardComponent onClick={mockClickFunction}>
        Sample Test Component
      </ClickableCardComponent>,
    );
    fireEvent.click(getByText('Sample Test Component'));

    expect(mockClickFunction).toHaveBeenCalled();
  });
});
