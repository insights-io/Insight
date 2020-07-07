import config from '../config';
import { getLocation } from '../utils';

fixture('_error').page(`${config.appBaseURL}/random-path`);

test('Should navigate to / and then to /login on 404 page', async (t) => {
  await t.expect(getLocation()).eql(`${config.appBaseURL}/login?dest=%2F`);
});
