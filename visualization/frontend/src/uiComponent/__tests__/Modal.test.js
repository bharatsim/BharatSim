import React from 'react';
import { render, within } from '@testing-library/react';

import { fireEvent } from '@testing-library/dom';
import Modal from '../Modal';

describe('<Modal />', () => {
  it('should match snapshot', () => {
    render(
      <Modal
        open
        handleClose={jest.fn()}
        title="Modal"
        actions={[
          { name: 'Apply', handleClick: jest.fn(), variant: 'contained', color: 'primary' },
        ]}
      >
        Hello this is modal
      </Modal>,
    );

    expect(document.querySelector('.MuiPaper-root')).toMatchSnapshot();
  });

  it('should match snapshot without actions', () => {
    render(
      <Modal open handleClose={jest.fn()} title="Modal">
        Hello this is modal
      </Modal>,
    );

    expect(document.querySelector('.MuiPaper-root')).toMatchSnapshot();
  });

  it('should call handle close callback on click of close icon button', () => {
    const handleClose = jest.fn();
    render(
      <Modal
        open
        handleClose={handleClose}
        title="Modal"
        actions={[
          { name: 'Apply', handleClick: jest.fn(), variant: 'contained', color: 'primary' },
        ]}
      >
        Hello this is modal
      </Modal>,
    );

    const modal = within(document.querySelector('.MuiPaper-root'));
    const closeIconButton = modal.getByTestId('button-icon-close');

    fireEvent.click(closeIconButton);

    expect(handleClose).toHaveBeenCalled();
  });

  it('should call action callback on click of first action button', () => {
    const handleApply = jest.fn();
    render(
      <Modal
        open
        handleClose={jest.fn()}
        title="Modal"
        actions={[
          { name: 'Apply', handleClick: handleApply, variant: 'contained', color: 'primary' },
        ]}
      >
        Hello this is modal
      </Modal>,
    );

    const modal = within(document.querySelector('.MuiPaper-root'));
    const closeIconButton = modal.getByTestId('button-Apply');

    fireEvent.click(closeIconButton);

    expect(handleApply).toHaveBeenCalled();
  });
});
