import React from 'react';
import { render, within } from '@testing-library/react';
import { fireEvent } from '@testing-library/dom';
import CreateNewDashboardModal from '../CreateNewDashboardModal';
import withThemeProvider from '../../../theme/withThemeProvider';

describe('<CreateNewDashboardModal />', () => {
  const CreateNewDashboardModalComponent = withThemeProvider(CreateNewDashboardModal);
  it('should match snapshot', () => {
    render(<CreateNewDashboardModalComponent isOpen closeModal={jest.fn()} onCreate={jest.fn} />);
    const container = document.querySelector('.MuiPaper-root');
    expect(container).toMatchSnapshot();
  });
  it('should change project name on change of input value', () => {
    const onCreateMock = jest.fn();
    render(
      <CreateNewDashboardModalComponent isOpen closeModal={jest.fn()} onCreate={onCreateMock} />,
    );

    const { getByLabelText, getByText } = within(document.querySelector('.MuiPaper-root'));
    const event = { target: { value: 'changed Value', id: 'project-title' } };

    fireEvent.change(getByLabelText('Project Title'), event);
    fireEvent.click(getByText('create'));

    expect(onCreateMock).toHaveBeenCalledWith({
      'dashboard-title': 'Untitled Dashboard',
      'project-title': 'changed Value',
    });
  });
});
