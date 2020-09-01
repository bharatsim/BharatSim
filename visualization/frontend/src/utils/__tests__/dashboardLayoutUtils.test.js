import React from 'react';
import { render } from '@testing-library/react';
import { renderElement, getNewWidgetLayout } from '../dashboardLayoutUtils';

jest.mock('../../component/charts/renderChart', () => ({
  __esModule: true,
  default: (chartType, props) => (
    <div>
      Chart:
      {chartType}
      <span>
        {/* eslint-disable-next-line no-undef */}
        {mockPropsCapture(props)}
      </span>
    </div>
  ),
}));

describe('Dashboard layout utils', () => {
  describe('Create element', () => {
    it('should provide element with data-grid', () => {
      const element = renderElement({ layout: { i: 'id-1' }, chartType: 'Linechart', config: {} });
      const { container } = render(<>{element}</>);

      expect(container).toMatchSnapshot();
    });
  });

  describe('getNewWidgetLayout', () => {
    it('should provide layout for newly added widget', () => {
      const numberOfWidgetAdded = 2;
      const cols = 12;
      const count = 2;

      const layout = getNewWidgetLayout(numberOfWidgetAdded, cols, count);

      expect(layout).toEqual({
        i: 'widget-2',
        x: 0,
        y: Infinity,
        w: 6,
        h: 2,
      });
    });
  });
});
