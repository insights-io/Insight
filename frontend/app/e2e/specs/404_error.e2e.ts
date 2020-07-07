import config from '../config';
import { getLocation } from '../utils';

fixture('404_error').page(`${config.appBaseURL}/random-path`);

test('Should end up on login page after 404', async (t) => {
  await t.expect(getLocation()).eql(`${config.appBaseURL}/login?dest=%2F`);
});
