import React from 'react';
import { render, within } from '@testing-library/react';
import { fireEvent } from '@testing-library/dom';
import ProjectHomeScreen from '../ProjectHomeScreen';
import withThemeProvider from '../../../theme/withThemeProvider';

describe('<ProjectHomeScreenComponent />', () => {
  const ProjectHomeScreenComponent = withThemeProvider(ProjectHomeScreen);
  it('should match snapshot', () => {
    const { container } = render(<ProjectHomeScreenComponent />);
    expect(container).toMatchSnapshot();
  });
  it('should open new dashboard popup onclick of card', () => {
    const { getByText } = render(<ProjectHomeScreenComponent />);
    fireEvent.click(getByText('Click here to create your first dashboard.'));

    const container = within(document.querySelector('.MuiPaper-root'));

    expect(container.queryByText('New Dashboard')).toBeInTheDocument();
  });
});
