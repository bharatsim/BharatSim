import { renderHook, act } from '@testing-library/react-hooks';
import useFetchAndTransformChartData from '../useFetchAndTransformChartData';
import useFetch from '../useFetch';
import { chartConfig } from '../../component/charts/chartConfig';

jest.mock('../useFetch');

describe('Use FetchAndTransformChartData hook', () => {
  beforeEach(() => {
    useFetch.mockReturnValue({ data: { exposed: [2, 3], hour: [1, 2] } });
  });
  it('should return isOpen by default false', () => {
    const { result } = renderHook(() =>
      useFetchAndTransformChartData({
        xAxis: 'hour',
        yAxis: [{ name: 'exposed', type: 'number' }],
        dataSource: 'datasource',
      }),
    );
    const expectedResult = {
      labels: [1, 2],
      datasets: [
        {
          ...chartConfig,
          label: 'exposed',
          borderColor: '#4dc9f6',
          backgroundColor: '#4dc9f6',
          data: [2, 3],
        },
      ],
    };
    expect(result.current).toEqual(expectedResult);
  });
});
