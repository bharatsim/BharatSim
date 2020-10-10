import React from 'react';
import { render } from '@testing-library/react';
import { waitFor } from '@testing-library/dom';
import Home from '../Home';
import { api } from '../../../utils/api';

jest.mock('../../../utils/api', () => ({
  api: {
    getProjects: jest.fn(),
  },
}));

describe('Home', () => {
  it('should match snapshot for new user', async () => {
    api.getProjects.mockResolvedValue({ projects: [] });
    const { container, findByText } = render(<Home />);

    await findByText('Welcome to BharatSim');
    await waitFor(() => expect(container).toMatchSnapshot());
  });
  it('should match snapshot for existing users', async () => {
    api.getProjects.mockResolvedValue({ projects: [{ name: 'project1', _id: '1' }] });
    const { container, findByText } = render(<Home />);

    await findByText('project1');

    expect(container).toMatchSnapshot();
  });
});
