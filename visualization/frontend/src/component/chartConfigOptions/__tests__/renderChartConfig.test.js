import { render } from '@testing-library/react';

import { chartConfigOptions } from '../../../constants/chartConfigOptions';
import renderChartConfig from '../renderChartConfig';

describe('Render chart config', () => {
  it('should render chart config with provided props', () => {
    const chartConfig = [chartConfigOptions.X_AXIS, chartConfigOptions.Y_AXIS];
    const chartConfigOptionProps = {
      headers: [
        { name: 'a', type: 'number' },
        { name: 'b', type: 'number' },
        { name: 'c', type: 'number' },
      ],
      updateConfigState: jest.fn(),
      errors: { xAxis: '', yAxis: '' },
      values: {},
    };

    const ChartConfigComponent = renderChartConfig(chartConfig, chartConfigOptionProps);
    const { getByTestId } = render(ChartConfigComponent);

    expect(getByTestId('dropdown-x')).toBeInTheDocument();
    expect(getByTestId('dropdown-y')).toBeInTheDocument();
  });
});
