import React from 'react';
import { render } from '@testing-library/react';
import NewUserHomeScreen from '../NewUserHomeScreen';

const mockHistoryPush = jest.fn();

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useHistory: () => ({
    push: mockHistoryPush,
  }),
}));

describe('NewUserHomeScreen', () => {
  it('should match snapshot', () => {
    const { container } = render(<NewUserHomeScreen />);

    expect(container).toMatchSnapshot();
  });
  it('should navigate to the createNewProject Url', () => {
    const { getByText } = render(<NewUserHomeScreen />);
    const createNewProjectButton = getByText('Create new Project');

    createNewProjectButton.click();

    expect(mockHistoryPush).toHaveBeenCalledWith('/projects/createNew');
  });
});
