// eslint-disable-next-line import/no-extraneous-dependencies
import { fireEvent, within } from '@testing-library/react';

export function selectDropDownOption(container, dropDownId, optionId) {
  const dropDown = container.getByTestId(dropDownId);
  fireEvent.mouseDown(within(dropDown).getByRole('button'));
  const options = within(
    within(document.getElementById(`menu-${dropDownId}`)).getByRole('listbox'),
  );
  fireEvent.click(options.getByTestId(`${dropDownId}-${optionId}`));
}
