import React from 'react';
import { render } from '@testing-library/react';
import { renderElement, getNewWidgetLayout } from '../utils';

jest.mock('../../charts/renderChart', () => ({
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
      const element = renderElement({ i: 'id-1', chartType: 'Linechart', config: {} });
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
