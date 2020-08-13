import { datasourceValidator, getChartConfigValidator } from '../validators';

describe('Validators', () => {
  describe('X axis validator', () => {
    it('should provide message if x axis value is not present', () => {
      const validator = getChartConfigValidator('xAxis');

      expect(validator('')).toEqual('Please select value for x axis');
    });

    it('should provide empty message if x axis value is present', () => {
      const validator = getChartConfigValidator('xAxis');

      expect(validator('test')).toEqual('');
    });

    it('should provide message if x axis value is undefined', () => {
      const validator = getChartConfigValidator('xAxis');

      expect(validator()).toEqual('Please select value for x axis');
    });
  });

  describe('Y axis validator', () => {
    it('should provide message if y axis value is not present', () => {
      const validator = getChartConfigValidator('yAxis');

      expect(validator('')).toEqual('Please select value for y axis');
    });

    it('should provide empty message if y axis value is present', () => {
      const validator = getChartConfigValidator('xAxis');

      expect(validator('test')).toEqual('');
    });

    it('should provide message if y axis value undefined', () => {
      const validator = getChartConfigValidator('yAxis');

      expect(validator()).toEqual('Please select value for y axis');
    });
  });

  describe('datasource validator', () => {
    it('should provide message if datasource value is not present', () => {
      expect(datasourceValidator('')).toEqual('Please select data source');
    });

    it('should provide empty message if datasource value is present', () => {
      expect(datasourceValidator('test')).toEqual('');
    });

    it('should provide message if datasource value is undefined', () => {
      expect(datasourceValidator()).toEqual('Please select data source');
    });
  });
});
