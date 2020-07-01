import React from 'react';
import { render } from '@testing-library/react';
import { createElement, getNewWidgetLayout } from '../utils';

jest.mock('../../lineChart/LineChart.js', () => () => <>Hello, Welcome</>);

describe('Dashboard layout utils', () => {
  describe('Create element', () => {
    it('should provide element with data-grid', () => {
      const element = createElement({ i: 'id-1' });
      const { container } = render(<>{element}</>);

      expect(container).toMatchSnapshot();
    });
  });

  describe('getNewWidgetLayout', () => {
    it('should provide layout for newly added widget', () => {
      const numberOfWidgetAdded = 2;
      const cols = 12;
      const count = 1;

      const layout = getNewWidgetLayout(numberOfWidgetAdded, cols, count);

      expect(layout).toEqual({
        i: 'widget-1',
        x: 4,
        y: Infinity,
        w: 2,
        h: 2,
      });
    });
  });
});
