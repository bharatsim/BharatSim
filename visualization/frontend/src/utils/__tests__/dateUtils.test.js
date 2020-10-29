import { formatDate } from '../dateUtils';

describe('Date utils', () => {
  it('should provide formatted date', () => {
    const date = new Date('2020-10-29T09:17:09.146Z');

    expect(formatDate(date)).toEqual('29-Oct-2020 at 2:47 PM');
  });

  it('should provide formatted date for am', () => {
    const date = new Date('2020-10-29T01:17:09.146Z');

    expect(formatDate(date)).toEqual('29-Oct-2020 at 6:47 AM');
  });
  it('should provide -- if date is invalids', () => {
    const date = new Date(undefined);

    expect(formatDate(date)).toEqual('--');
  });
});
