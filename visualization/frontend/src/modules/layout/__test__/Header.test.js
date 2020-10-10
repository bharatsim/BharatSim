import React from 'react';
import { render } from '@testing-library/react';
import { fireEvent } from '@testing-library/dom';
import withHeader from '../withHeader';
import withThemeProvider from '../../../theme/withThemeProvider';

function DummyController() {
  return <div>DummyController</div>;
}

const mockHistoryPush = jest.fn();

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useHistory: () => ({
    push: mockHistoryPush,
  }),
}));

const Component = withThemeProvider(withHeader(DummyController));

describe('<Header />', () => {
  it('should match snapshot', () => {
    const { container } = render(<Component />);

    expect(container).toMatchSnapshot();
  });

  it('should get to landing on click of logo', function () {
    const { getByAltText } = render(<Component />);
    fireEvent.click(getByAltText('logo'));

    expect(mockHistoryPush).toHaveBeenCalledWith('/');
  });
});
