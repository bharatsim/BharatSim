import React from "react";
import {fireEvent, render, within, wait} from "@testing-library/react";

import ChartConfigModal from "../ChartConfigModal";
import useFetch from "../../../hook/useFetch";
import labels from '../../../constants/labels'

jest.mock('../../../hook/useFetch')
describe('<ChartConfigModal />', function () {
  const mockOnCancel = jest.fn()
  const mockOnOK = jest.fn()

  beforeEach(()=>{
    useFetch.mockReturnValue({headers: ["x-header", "y-header"]})
  })

  it('should match a snapshot for <ChartConfigModal />', function () {
    const {container} = render(<ChartConfigModal />);

    expect(container).toMatchSnapshot();
  });

  it('should add', async () => {
    const {container, getByText,debug, getByTestId, getByRole, waitForNextUpdate} = render(<ChartConfigModal open={true} onOk={mockOnOK} onCancel={mockOnCancel} />);

    const selectX = getByTestId('select-x')
    const selectY = getByTestId('select-y')

    fireEvent.click(selectX);

    await wait(()=>{
      const listbox = within(getByRole('listbox'));
      console.log(listbox)
    })
    // const menuXColumn = ;
    // console.log(menuXColumn)
    //
    // fireEvent.click(menuXColumn);
    // fireEvent.click(selectY);
    // const menuYColumn = getByText(/y-header/i);
    // fireEvent.click(menuYColumn);
    // fireEvent.click(getByText(/ok/i));
    //
    // expect(mockOnOK).toHaveBeenCalledWith({config: {xColumn: 'x-column', yColumn:''}});
  },10000);

});