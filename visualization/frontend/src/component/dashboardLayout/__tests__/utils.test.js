import React from "react";
import {render} from '@testing-library/react'
import {createElement, getInitialLayout} from "../utils";

jest.mock("../../lineChart/LineChart.js", () => () => {
  return <>Hello, Welcome</>
})

describe('Dashboard layout utils', function () {
  describe("Create element", () => {
    it('should provide element with data-grid', () => {
      const element = createElement({i: 'id-1'});
      const {container} = render(<>{element}</>);

      expect(container).toMatchSnapshot();
    })
  })

  describe("getInitialLayout", () => {
    it('should provide element with data-grid', () => {
      const layout = getInitialLayout();

      expect(layout).toEqual([{
        i: '0',
        x: 0,
        y: 0,
        w: 2,
        h: 2,
      }]);
    })
  })
});