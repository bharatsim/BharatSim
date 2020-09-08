import { renderHook } from '@testing-library/react-hooks';
import useFetchAndTransformChartData from '../useFetchAndTransformChartData';
import useFetch from '../useFetch';
import { chartStyleConfig } from '../../component/charts/chartStyleConfig';

jest.mock('../useFetch');

describe('Use FetchAndTransformChartData hook', () => {
  beforeEach(() => {
    useFetch.mockReturnValue({
      data: { data: { exposed: [2, 3], hour: [1, 2] } },
      loadingState: 'SUCCESS',
    });
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
      data: {
        labels: [1, 2],
        datasets: [
          {
            ...chartStyleConfig,
            label: 'exposed',
            borderColor: '#4dc9f6',
            backgroundColor: '#4dc9f6',
            data: [2, 3],
          },
        ],
      },
      loadingState: 'SUCCESS',
    };

    expect(result.current).toEqual(expectedResult);
  });
});
