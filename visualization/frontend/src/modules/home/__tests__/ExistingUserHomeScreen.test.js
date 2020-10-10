import React from 'react';
import { render } from '@testing-library/react';
import { fireEvent } from '@testing-library/dom';
import ExistingUserHomeScreen from '../ExistingUserHomeScreen';

const mockHistoryPush = jest.fn();

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useHistory: () => ({
    push: mockHistoryPush,
  }),
}));

describe('Existing User Home Screen', () => {
  it('should match snapshot for given projects', () => {
    const { container } = render(
      <ExistingUserHomeScreen recentProjects={[{ _id: '1', name: 'project1' }]} />,
    );

    expect(container).toMatchSnapshot();
  });
  it('should navigate to project on click of project card', () => {
    const { getByText } = render(
      <ExistingUserHomeScreen recentProjects={[{ _id: '1', name: 'project1' }]} />,
    );

    fireEvent.click(getByText('project1'));

    expect(mockHistoryPush).toHaveBeenCalledWith('/projects/1');
  });
  it('should create new project on click of add new button', () => {
    const { getByText } = render(
      <ExistingUserHomeScreen recentProjects={[{ _id: '1', name: 'project1' }]} />,
    );

    fireEvent.click(getByText('Add New'));

    expect(mockHistoryPush).toHaveBeenCalledWith('/projects/create');
  });
});
