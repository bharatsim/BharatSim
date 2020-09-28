import React from 'react';
import { render } from '@testing-library/react';
import App from '../App';

jest.mock('../utils/fetch');

jest.mock('../AppRoute', () => () => <>App Routes</>);

describe('<App />', () => {
  it('should match snapshot', () => {
    const { container } = render(<App />);

    expect(container).toMatchSnapshot();
  });
});
