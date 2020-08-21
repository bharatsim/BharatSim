import React from 'react';
import { render } from '@testing-library/react';
import ButtonGroup from '../ButtonGroup';

describe('<Buttongroup />', () => {
  it('should match snapshot', () => {
    const { container } = render(
      <ButtonGroup>
        <button type="button">click here</button>
        <button type="button">click here</button>
      </ButtonGroup>,
    );

    expect(container).toMatchSnapshot();
  });
});
