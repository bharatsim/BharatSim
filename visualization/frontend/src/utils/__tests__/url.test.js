import { url } from '../url';

describe('URL', () => {
  it('should provide a url', () => {
    expect(url).toMatchSnapshot();
  });
});
