import { render } from '@testing-library/react';
import React from 'react';

import Text from '../Text';
import useFetch from '../../../hook/useFetch';

jest.mock('../../../hook/useFetch');

describe('<Text />', () => {
  beforeEach(() => {
    useFetch.mockReturnValue('hello, Welcome to bharatsim');
  });

  it('should have fetched text in <Text /> component', () => {
    const { getByText } = render(<Text />);

    const element = getByText(/hello, Welcome to bharatsim/i);
    expect(element).toBeInTheDocument();
  });

  it('should match a snapshot for <Text />', () => {
    const { container } = render(<Text />);

    expect(container).toMatchSnapshot();
  });
});
