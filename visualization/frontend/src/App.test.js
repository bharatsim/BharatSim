import React from 'react';
import { render } from '@testing-library/react';
import App from './App';

describe('<App />', function () {
  it('renders learn react link',()=>{
    const { getByText } = render(<App />);
    const linkElement = getByText(/Welcome to BharatSim Visualization/i);
    expect(linkElement).toBeInTheDocument();
  })
});