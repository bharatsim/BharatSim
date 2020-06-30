import {url} from '../url'
describe('URL',  () => {
  it('should provide a url', function () {
    expect(url).toMatchSnapshot();
  });
});