import React from "react";
import {fireEvent, render} from "@testing-library/react";

import DashboardLayout from "../DashboardLayout";
jest.mock("../../text/Text",()=>()=><>Hello text</>);

describe('<DashboardLayout />', function () {
  it('should match a snapshot for <DashboardLayout />', function () {
    const {container} = render(<DashboardLayout />);

    expect(container).toMatchSnapshot();
  });

  it('should add new widget on click of add widget button', function () {
    const {container, getByText, getByTestId} = render(<DashboardLayout />);
    const addWidgetButton = getByText(/Add Widget/i);

    fireEvent.click(addWidgetButton);

    const newWidget= getByTestId("n0");
    expect(newWidget).toBeInTheDocument();
  });
});