import config from '../config';
import { LoginPage } from '../pages';
import { getLocation } from '../utils';

fixture('404_error').page(`${config.appBaseURL}/random-path`);

test('User should end up on login page after 404', async (t) => {
  await t.expect(getLocation()).eql(`${LoginPage.path}?redirect=%2F`);
});
