import React from "react";
import {render} from "@testing-library/react";

import DashboardLayout from "../DashboardLayout";
import Text from "../../text/Text";
jest.mock("../../text/Text",()=>()=><>Hello text</>);

describe('<DashboardLayout />', function () {
  it('should match a snapshot for <DashboardLayout />', function () {
    const {container} = render(<DashboardLayout />);

    expect(container).toMatchSnapshot();
  });
});