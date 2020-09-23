import React from 'react';
import { render } from '@testing-library/react';
import Home from '../Home';

const mockHistoryPush = jest.fn();

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useHistory: () => ({
    push: mockHistoryPush,
  }),
}));

describe('Home', () => {
  it('should match snapshot', () => {
    const { container } = render(<Home />);
    expect(container).toMatchSnapshot();
  });

  it('should navigate to the createNewProject Url', function () {
    const { getByText } = render(<Home />);
    const createNewProjectButton = getByText('Create new Project');

    createNewProjectButton.click();

    expect(mockHistoryPush).toHaveBeenCalledWith('/project/createNewProject');
  });
});
