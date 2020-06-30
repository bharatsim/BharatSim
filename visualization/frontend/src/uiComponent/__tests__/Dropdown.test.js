import React from "react";
import {fireEvent, render, within} from "@testing-library/react";

import Dropdown from "../Dropdown";

describe('<Dropdown />', () => {
  let props = {
    options: ['one', 'two', 'three'],
    id: "numbers",
    label: "select number",
    onChange: jest.fn()
  }

  it('Should match a snapshot', () => {
    const {container} = render(<Dropdown {...props}/>)

    expect(container).toMatchSnapshot();
  })

  it('Should create a dropdown with provided options', () => {
    const {getByRole} = render(<Dropdown {...props}/>)

    const button = getByRole('button');
    fireEvent.mouseDown(button);

    expect(document.querySelector("ul")).toMatchSnapshot();
  })

  it('Should change selected value after option selected', () => {
    const {getByRole} = render(<Dropdown {...props}/>)

    const button = getByRole('button');
    fireEvent.mouseDown(button);

    const optionList = within(document.querySelector("ul"));
    const optionOne = optionList.getByText(/Two/i);
    fireEvent.click(optionOne);

    expect(button).toHaveTextContent(/Two/i);
  })

  it('Should call onChange callback with selected option', () => {
    const {getByRole} = render(<Dropdown {...props}/>)

    const button = getByRole('button');
    fireEvent.mouseDown(button);

    const optionList = within(document.querySelector("ul"));
    const optionOne = optionList.getByText(/Two/i);
    fireEvent.click(optionOne);

    expect(props.onChange).toHaveBeenCalledWith("two");
  })
});