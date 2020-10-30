import { formatDate } from '../dateUtils';

describe('Date utils', () => {
  it('should provide formatted date', () => {
    const date = new Date('Fri Oct 20 2020 22:39:07 GMT+0530');

    expect(formatDate(date)).toEqual('20-Oct-2020 at 5:09 PM');
  });

  it('should provide formatted date for am', () => {
    const date = new Date('Fri Oct 20 2020 15:39:07 GMT+0530');

    expect(formatDate(date)).toEqual('20-Oct-2020 at 10:09 AM');
  });
  it('should provide formatted date for if minute are two digit', () => {
    const date = new Date('Fri Oct 20 2020 15:45:07 GMT+0530');

    expect(formatDate(date)).toEqual('20-Oct-2020 at 10:15 AM');
  });
  it('should provide -- if date is invalids', () => {
    const date = new Date(undefined);

    expect(formatDate(date)).toEqual('--');
  });
});
