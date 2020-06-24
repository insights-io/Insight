import { getAllByText } from '@testing-library/testcafe';

import { login } from '../utils';
import config from '../config';

fixture('/sessions').page(`${config.appBaseURL}/login `);

test('Should be able to see sessions for Insight logged in user', async (t) => {
  await login(t, {
    email: config.insightUserEmail,
    password: config.insightUserPassword,
  });

  await t
    .expect(getAllByText('172.18.0.1').visible)
    .ok('Correct IP address is displayed')
    .expect(getAllByText('less than 5 seconds ago').visible)
    .ok('Newly created session is dispalyed');
});
