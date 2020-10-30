import React from 'react';
import { render } from '@testing-library/react';
import { fireEvent } from '@testing-library/dom';
import ProjectHeader from '../ProjectHeader';

const mockHistoryPush = jest.fn();

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useParams: jest.fn(),
  useHistory: () => ({
    push: mockHistoryPush,
  }),
}));

describe('<ProjectHeader />', () => {
  it('should match snapshot', () => {
    const { container } = render(<ProjectHeader>Title</ProjectHeader>);

    expect(container).toMatchSnapshot();
  });

  it('should navigate to recent projects on click of back to recent button', () => {
    const { getByText } = render(<ProjectHeader>Title</ProjectHeader>);

    fireEvent.click(getByText('Back to recent projects'));

    expect(mockHistoryPush).toHaveBeenCalledWith('/');
  });
});
