import { v4 as uuid } from 'uuid';
import { getByText } from '@testing-library/testcafe';

import config from '../config';
import { signUpAndLogin } from '../utils';

fixture('/').page(config.appBaseURL);

test('Should be able to log in to app', async (t) => {
  await signUpAndLogin(t, {
    fullName: 'Matej Snuderl',
    company: 'Insight',
    email: `ematej.snuderl+${uuid()}@gmail.com`,
    password: uuid(),
  });

  await t
    .expect(getByText('THIS IS WHERE THE MAGIC WILL HAPPEN').visible)
    .ok('Should be signed in to app');
});
